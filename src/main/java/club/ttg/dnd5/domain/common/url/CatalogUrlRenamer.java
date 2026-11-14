package club.ttg.dnd5.domain.common.url;

import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.util.SlugifyUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static club.ttg.dnd5.domain.common.url.UrlTokenRewriter.escapeLike;
import static club.ttg.dnd5.domain.common.url.UrlTokenRewriter.quote;

/**
 * Смена адресов записей источника вслед за его акронимом: {@code fireball-phb} →
 * {@code fireball-ph}.
 *
 * <p>Меняются только записи этого источника, чей адрес оканчивается на
 * {@code -<слаг прежнего акронима>}; адрес, заданный когда-то вручную, остаётся. Связи
 * между таблицами переезжают каскадом внешних ключей, адреса в тексте и JSON — через
 * {@link UrlTokenRewriter}. Прежние адреса записываются в {@code entity_url_history}
 * (по ним страницы продолжают открываться), первоначальные для VTTG — в
 * {@code vttg_url_alias}.</p>
 */
@Component
public class CatalogUrlRenamer {
    /** Сколько конфликтов перечислять в ошибке. */
    private static final int CONFLICTS_SHOWN = 20;
    private static final int CHUNK = 500;

    @PersistenceContext
    private EntityManager entityManager;
    private final UrlTokenRewriter rewriter;

    public CatalogUrlRenamer(UrlTokenRewriter rewriter) {
        this.rewriter = rewriter;
    }

    CatalogUrlRenamer(EntityManager entityManager, UrlTokenRewriter rewriter) {
        this.entityManager = entityManager;
        this.rewriter = rewriter;
    }

    /**
     * Смена адреса одной записи.
     *
     * @param table   таблица каталога
     * @param section раздел сайта
     * @param oldUrl  прежний адрес
     * @param newUrl  новый адрес
     */
    public record UrlChange(String table, String section, String oldUrl, String newUrl) {
    }

    /**
     * Меняет адреса записей источника. Вызывается после смены акронима, внутри той же
     * транзакции: записи уже ссылаются на источник с новым акронимом.
     *
     * @param oldAcronym прежний акроним
     * @param newAcronym новый акроним (источник уже переименован)
     * @param username   кто меняет; {@code null}, если неизвестен
     * @return сменённые адреса
     * @throws EntityExistException если новый адрес занят или прежний неоднозначен — ничего не меняется
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public List<UrlChange> rename(String oldAcronym, String newAcronym, String username) {
        String oldTail = tail(oldAcronym);
        String newTail = tail(newAcronym);
        if (oldTail == null || newTail == null || oldTail.equals(newTail)) {
            return List.of();
        }
        List<UrlChange> changes = collect(newAcronym, oldTail, newTail);
        if (changes.isEmpty()) {
            return changes;
        }
        checkConflicts(changes);

        entityManager.flush();
        Map<String, String> replacements = new LinkedHashMap<>();
        for (UrlChange change : changes) {
            entityManager.createNativeQuery(
                            "update " + quote(change.table()) + " set url = :newUrl where url = :oldUrl")
                    .setParameter("newUrl", change.newUrl())
                    .setParameter("oldUrl", change.oldUrl())
                    .executeUpdate();
            moveVttgExport(change);
            rememberVttgOriginal(change);
            rememberOldUrl(change, username);
            replacements.put(change.oldUrl(), change.newUrl());
        }
        rewriter.rewrite(replacements, oldTail);
        return changes;
    }

    /** Хвост адреса для акронима: {@code PHB} → {@code -phb}; {@code null}, если слага нет. */
    static String tail(String acronym) {
        if (!StringUtils.hasText(acronym)) {
            return null;
        }
        String slug = SlugifyUtil.getSlug(acronym.trim());
        return StringUtils.hasText(slug) ? "-" + slug : null;
    }

    @SuppressWarnings("unchecked")
    private List<UrlChange> collect(String acronym, String oldTail, String newTail) {
        List<UrlChange> changes = new ArrayList<>();
        CatalogTables.SECTIONS.forEach((table, section) -> {
            List<String> urls = entityManager.createNativeQuery(
                            "select url from " + quote(table)
                                    + " where source = :acronym and url like :tail escape '!' order by url")
                    .setParameter("acronym", acronym)
                    .setParameter("tail", "%" + escapeLike(oldTail))
                    .getResultList();
            for (String url : urls) {
                String prefix = url.substring(0, url.length() - oldTail.length());
                if (!prefix.isEmpty()) {
                    changes.add(new UrlChange(table, section, url, prefix + newTail));
                }
            }
        });
        return changes;
    }

    /**
     * Отказ, если:
     * <ul>
     *   <li>новый адрес уже есть у записи любого раздела (адреса в тексте не несут раздела,
     *       и замена стала бы неоднозначной);</li>
     *   <li>новый адрес числится прежним у другой записи — по нему открывается она;</li>
     *   <li>прежний адрес есть у записи другого раздела, которая не переименовывается.</li>
     * </ul>
     */
    private void checkConflicts(List<UrlChange> changes) {
        Set<String> newUrls = new HashSet<>();
        Set<String> oldUrls = new HashSet<>();
        Set<String> renamed = new HashSet<>();
        Map<String, UrlChange> byNewUrl = new HashMap<>();
        for (UrlChange change : changes) {
            newUrls.add(change.newUrl());
            oldUrls.add(change.oldUrl());
            renamed.add(change.table() + "/" + change.oldUrl());
            byNewUrl.put(change.section() + "/" + change.newUrl(), change);
        }
        List<String> conflicts = new ArrayList<>();
        CatalogTables.SECTIONS.forEach((table, section) -> {
            for (String url : liveUrls(table, newUrls)) {
                conflicts.add("адрес «" + url + "» уже занят в разделе " + section);
            }
            for (String url : liveUrls(table, oldUrls)) {
                if (!renamed.contains(table + "/" + url)) {
                    conflicts.add("адрес «" + url + "» есть в разделе " + section + " у записи другого источника");
                }
            }
        });
        for (Object[] row : historyByOldUrls(newUrls)) {
            String section = (String) row[0];
            String oldUrl = (String) row[1];
            String current = (String) row[2];
            UrlChange back = byNewUrl.get(section + "/" + oldUrl);
            boolean renamedBack = back != null && back.oldUrl().equals(current);
            if (!renamedBack) {
                conflicts.add("адрес «" + oldUrl + "» числится прежним у записи «" + current
                        + "» раздела " + section);
            }
        }
        if (!conflicts.isEmpty()) {
            List<String> shown = conflicts.size() > CONFLICTS_SHOWN ? conflicts.subList(0, CONFLICTS_SHOWN) : conflicts;
            String more = conflicts.size() > CONFLICTS_SHOWN ? " и ещё " + (conflicts.size() - CONFLICTS_SHOWN) : "";
            throw new EntityExistException("Акроним не сменён: адреса записей конфликтуют — "
                    + String.join("; ", shown) + more);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> liveUrls(String table, Set<String> urls) {
        List<String> found = new ArrayList<>();
        for (List<String> chunk : chunks(urls)) {
            found.addAll(entityManager.createNativeQuery(
                            "select url from " + quote(table) + " where url in (:urls)")
                    .setParameter("urls", chunk)
                    .getResultList());
        }
        return found;
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> historyByOldUrls(Set<String> urls) {
        List<Object[]> found = new ArrayList<>();
        for (List<String> chunk : chunks(urls)) {
            found.addAll(entityManager.createNativeQuery(
                            "select section, old_url, new_url from entity_url_history where old_url in (:urls)")
                    .setParameter("urls", chunk)
                    .getResultList());
        }
        return found;
    }

    /** Кэш выгрузки VTTG переезжает на новый адрес: payload от адреса не зависит ({@code vttg_url_alias}). */
    private void moveVttgExport(UrlChange change) {
        entityManager.createNativeQuery("delete from vttg_export where url = :newUrl")
                .setParameter("newUrl", change.newUrl())
                .executeUpdate();
        entityManager.createNativeQuery("update vttg_export set url = :newUrl where url = :oldUrl")
                .setParameter("newUrl", change.newUrl())
                .setParameter("oldUrl", change.oldUrl())
                .executeUpdate();
    }

    /** Первоначальный адрес для VTTG переходит на новый адрес записи. */
    @SuppressWarnings("unchecked")
    private void rememberVttgOriginal(UrlChange change) {
        List<String> known = entityManager.createNativeQuery(
                        "select original_url from vttg_url_alias where url = :oldUrl")
                .setParameter("oldUrl", change.oldUrl())
                .getResultList();
        entityManager.createNativeQuery("delete from vttg_url_alias where url = :oldUrl")
                .setParameter("oldUrl", change.oldUrl())
                .executeUpdate();
        String original = known.isEmpty() ? change.oldUrl() : known.get(0);
        if (!original.equals(change.newUrl())) {
            entityManager.createNativeQuery("""
                            insert into vttg_url_alias (url, original_url) values (:newUrl, :original)
                            on conflict (url) do update set original_url = excluded.original_url
                            """)
                    .setParameter("newUrl", change.newUrl())
                    .setParameter("original", original)
                    .executeUpdate();
        }
    }

    /** Прежний адрес и все ещё более старые ведут на новый: цепочки схлопываются. */
    private void rememberOldUrl(UrlChange change, String username) {
        entityManager.createNativeQuery(
                        "update entity_url_history set new_url = :newUrl where section = :section and new_url = :oldUrl")
                .setParameter("newUrl", change.newUrl())
                .setParameter("section", change.section())
                .setParameter("oldUrl", change.oldUrl())
                .executeUpdate();
        entityManager.createNativeQuery(
                        "delete from entity_url_history where section = :section and old_url = :newUrl")
                .setParameter("section", change.section())
                .setParameter("newUrl", change.newUrl())
                .executeUpdate();
        entityManager.createNativeQuery("""
                        insert into entity_url_history (section, old_url, new_url, changed_at, changed_by)
                        values (:section, :oldUrl, :newUrl, :changedAt, :changedBy)
                        on conflict (section, old_url) do update
                            set new_url = excluded.new_url, changed_at = excluded.changed_at,
                                changed_by = excluded.changed_by
                        """)
                .setParameter("section", change.section())
                .setParameter("oldUrl", change.oldUrl())
                .setParameter("newUrl", change.newUrl())
                .setParameter("changedAt", OffsetDateTime.now())
                .setParameter("changedBy", username)
                .executeUpdate();
    }

    private static List<List<String>> chunks(Set<String> values) {
        List<String> all = new ArrayList<>(values);
        List<List<String>> chunks = new ArrayList<>();
        for (int i = 0; i < all.size(); i += CHUNK) {
            chunks.add(all.subList(i, Math.min(all.size(), i + CHUNK)));
        }
        return chunks;
    }
}

package club.ttg.dnd5.domain.common.url;

import club.ttg.dnd5.exception.EntityExistException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Логика смены адресов на поддельной базе: какие записи переименовываются, когда
 * переименование отменяется и что пишется в таблицы прежних адресов. Сами запросы
 * проверены на Postgres отдельно.
 */
class CatalogUrlRenamerTest {
    private final EntityManager entityManager = mock(EntityManager.class);
    private final UrlTokenRewriter rewriter = mock(UrlTokenRewriter.class);
    private final CatalogUrlRenamer renamer = new CatalogUrlRenamer(entityManager, rewriter);

    /** Таблица → адреса записей нового акронима (выборка кандидатов). */
    private final Map<String, List<String>> sourceUrls = new HashMap<>();
    /** Таблица → все живые адреса (проверка занятости). */
    private final Map<String, List<String>> liveUrls = new HashMap<>();
    /** Строки entity_url_history: раздел, прежний, текущий. */
    private final List<Object[]> history = new ArrayList<>();
    /** Выполненные изменения: запрос и параметры. */
    private final List<Map.Entry<String, Map<String, Object>>> updates = new ArrayList<>();
    /** Выполненные выборки: запрос и параметры. */
    private final List<Map.Entry<String, Map<String, Object>>> selects = new ArrayList<>();

    @BeforeEach
    void setUp() {
        when(entityManager.createNativeQuery(anyString())).thenAnswer(invocation -> query(invocation.getArgument(0)));
    }

    /** Меняются только адреса с хвостом прежнего акронима; ручной адрес остаётся. */
    @Test
    void renamesUrlsWithAcronymTail() {
        sourceUrls.put("spell", List.of("fireball-phb", "magic-missile-phb"));
        sourceUrls.put("item", List.of("dagger-phb"));
        liveUrls.put("spell", List.of("fireball-phb", "magic-missile-phb"));
        liveUrls.put("item", List.of("dagger-phb"));

        List<CatalogUrlRenamer.UrlChange> changes = renamer.rename("PHB", "PH", "admin");

        assertEquals(List.of(
                new CatalogUrlRenamer.UrlChange("spell", "spells", "fireball-phb", "fireball-ph"),
                new CatalogUrlRenamer.UrlChange("spell", "spells", "magic-missile-phb", "magic-missile-ph"),
                new CatalogUrlRenamer.UrlChange("item", "items", "dagger-phb", "dagger-ph")), changes);
        assertTrue(executed("update \"spell\" set url = :newUrl where url = :oldUrl",
                Map.of("newUrl", "fireball-ph", "oldUrl", "fireball-phb")));
        assertTrue(executed("insert into entity_url_history",
                Map.of("section", "spells", "oldUrl", "fireball-phb", "newUrl", "fireball-ph")));
        assertTrue(executed("insert into vttg_url_alias",
                Map.of("newUrl", "dagger-ph", "original", "dagger-phb")));
        verify(rewriter).rewrite(Map.of(
                "fireball-phb", "fireball-ph",
                "magic-missile-phb", "magic-missile-ph",
                "dagger-phb", "dagger-ph"), "-phb");
    }

    /** Кандидаты выбираются по хвосту {@code -phb}, записи «phb» без приставки нет. */
    @Test
    void candidateQueryUsesTail() {
        renamer.rename("PHB", "PH", null);

        assertTrue(selects.stream().anyMatch(select -> select.getKey().contains("where source = :acronym and url like :tail")
                && select.getValue().equals(Map.of("acronym", "PH", "tail", "%-phb"))));
        verify(rewriter, never()).rewrite(anyMap(), anyString());
    }

    /** Смена только регистра — слаг тот же, адреса не меняются. */
    @Test
    void caseOnlyRenameKeepsUrls() {
        assertEquals(List.of(), renamer.rename("phb", "PHB", null));
        verify(entityManager, never()).createNativeQuery(anyString());
    }

    /** Новый адрес занят — отказ с названием конфликта, ничего не меняется. */
    @Test
    void takenNewUrlCancelsRename() {
        sourceUrls.put("spell", List.of("fireball-phb"));
        liveUrls.put("spell", List.of("fireball-phb", "fireball-ph"));

        EntityExistException failure = assertThrows(EntityExistException.class,
                () -> renamer.rename("PHB", "PH", null));

        assertTrue(failure.getMessage().contains("fireball-ph"), failure.getMessage());
        assertTrue(updates.isEmpty());
        verify(rewriter, never()).rewrite(anyMap(), anyString());
    }

    /** Прежний адрес есть в другом разделе у записи, которая не переименовывается, — отказ. */
    @Test
    void ambiguousOldUrlCancelsRename() {
        sourceUrls.put("spell", List.of("light-phb"));
        liveUrls.put("spell", List.of("light-phb"));
        liveUrls.put("glossary", List.of("light-phb"));

        assertThrows(EntityExistException.class, () -> renamer.rename("PHB", "PH", null));
        assertTrue(updates.isEmpty());
    }

    /** Новый адрес числится прежним у другой записи — отказ. */
    @Test
    void newUrlKnownAsOldCancelsRename() {
        sourceUrls.put("spell", List.of("fireball-phb"));
        liveUrls.put("spell", List.of("fireball-phb"));
        history.add(new Object[]{"spells", "fireball-ph", "big-fireball-phb"});

        assertThrows(EntityExistException.class, () -> renamer.rename("PHB", "PH", null));
        assertTrue(updates.isEmpty());
    }

    /** Возврат к прежнему адресу той же записи разрешён. */
    @Test
    void renamingBackIsAllowed() {
        sourceUrls.put("spell", List.of("fireball-ph"));
        liveUrls.put("spell", List.of("fireball-ph"));
        history.add(new Object[]{"spells", "fireball-phb", "fireball-ph"});

        List<CatalogUrlRenamer.UrlChange> changes = renamer.rename("PH", "PHB", null);

        assertEquals("fireball-phb", changes.get(0).newUrl());
        assertTrue(executed("delete from entity_url_history where section = :section and old_url = :newUrl",
                Map.of("section", "spells", "newUrl", "fireball-phb")));
    }

    @Test
    void tailIsSlugOfAcronym() {
        assertEquals("-phb24", CatalogUrlRenamer.tail("PHB24"));
        assertEquals("-ua-as", CatalogUrlRenamer.tail("UA AS"));
        assertNull(CatalogUrlRenamer.tail("  "));
    }

    private boolean executed(String sqlFragment, Map<String, Object> params) {
        return updates.stream().anyMatch(update -> update.getKey().contains(sqlFragment)
                && update.getValue().entrySet().containsAll(params.entrySet()));
    }

    private Query query(String sql) {
        Map<String, Object> params = new HashMap<>();
        Query query = mock(Query.class);
        when(query.setParameter(anyString(), any())).thenAnswer(set -> {
            params.put(set.getArgument(0), set.getArgument(1));
            return query;
        });
        when(query.executeUpdate()).thenAnswer(run -> {
            updates.add(Map.entry(sql, params));
            return 1;
        });
        when(query.getResultList()).thenAnswer(run -> {
            selects.add(Map.entry(sql, params));
            return results(sql, params);
        });
        return query;
    }

    private List<?> results(String sql, Map<String, Object> params) {
        if (sql.contains("from entity_url_history")) {
            Collection<?> urls = (Collection<?>) params.get("urls");
            return history.stream().filter(row -> urls.contains(row[1])).toList();
        }
        if (sql.contains("from vttg_url_alias")) {
            return List.of();
        }
        String table = sql.substring(sql.indexOf("from \"") + 6, sql.indexOf('"', sql.indexOf("from \"") + 6));
        if (sql.contains("source = :acronym")) {
            String tail = ((String) params.get("tail")).substring(1);
            return sourceUrls.getOrDefault(table, List.of()).stream().filter(url -> url.endsWith(tail)).toList();
        }
        Collection<?> urls = (Collection<?>) params.get("urls");
        return liveUrls.getOrDefault(table, List.of()).stream().filter(urls::contains).toList();
    }
}

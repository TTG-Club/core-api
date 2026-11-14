package club.ttg.dnd5.domain.common.url;

import club.ttg.dnd5.util.UrlTokens;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Переписывает адреса записей во всех текстовых и JSON-колонках базы.
 *
 * <p>Адрес записи хранится строкой во множестве мест без внешних ключей: в механике и
 * разметке описаний каталога, в листах персонажей, в Ширме Мастера, в бастионах игроков,
 * в оценках, в сохранённых фильтрах. Перечислять их руками — значит пропустить следующее
 * новое, поэтому обходятся все колонки типа text / varchar / json / jsonb, кроме таблиц
 * из {@link #EXCLUDED_TABLES}. Адрес меняется только целым словом ({@link UrlTokens}).</p>
 */
@Component
public class UrlTokenRewriter {
    /**
     * Таблицы, которые не переписываются: история правок (снимки прошлого, к тому же
     * защищена от изменений триггером), служебные таблицы Liquibase, выгрузка VTTG (в ней
     * намеренно остаются первоначальные адреса) и сами таблицы прежних адресов.
     */
    static final Set<String> EXCLUDED_TABLES = Set.of(
            "entity_revision", "databasechangelog", "databasechangeloglock",
            "vttg_export", "vttg_url_alias", "entity_url_history");

    @PersistenceContext
    private EntityManager entityManager;

    public UrlTokenRewriter() {
    }

    UrlTokenRewriter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * @param replacements прежний адрес → новый
     * @param hint         подстрока, которая есть в каждом прежнем адресе (хвост акронима):
     *                     по ней база отбирает строки-кандидаты, точная замена — уже здесь
     * @return сколько значений изменено
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public int rewrite(Map<String, String> replacements, String hint) {
        if (replacements.isEmpty()) {
            return 0;
        }
        Pattern pattern = UrlTokens.pattern(replacements.keySet());
        String like = "%" + escapeLike(hint) + "%";
        int changed = 0;
        for (Object[] column : columns()) {
            String table = (String) column[0];
            String name = (String) column[1];
            String type = castType((String) column[2]);
            @SuppressWarnings("unchecked")
            List<Object[]> rows = entityManager.createNativeQuery(
                            "select cast(ctid as text), cast(" + quote(name) + " as text) from " + quote(table)
                                    + " where cast(" + quote(name) + " as text) like :like escape '!'")
                    .setParameter("like", like)
                    .getResultList();
            for (Object[] row : rows) {
                String value = (String) row[1];
                String replaced = UrlTokens.replace(value, replacements, pattern);
                if (replaced.equals(value)) {
                    continue;
                }
                entityManager.createNativeQuery(
                                "update " + quote(table) + " set " + quote(name) + " = cast(:value as " + type
                                        + ") where ctid = cast(:ctid as tid)")
                        .setParameter("value", replaced)
                        .setParameter("ctid", row[0])
                        .executeUpdate();
                changed++;
            }
        }
        return changed;
    }

    /** Колонки-кандидаты: таблица, колонка, тип. */
    @SuppressWarnings("unchecked")
    private List<Object[]> columns() {
        List<Object[]> columns = entityManager.createNativeQuery("""
                        select c.table_name, c.column_name, c.data_type
                        from information_schema.columns c
                        join information_schema.tables t
                          on t.table_schema = c.table_schema and t.table_name = c.table_name
                        where c.table_schema = current_schema()
                          and t.table_type = 'BASE TABLE'
                          and c.data_type in ('text', 'character varying', 'json', 'jsonb')
                        order by c.table_name, c.column_name
                        """)
                .getResultList();
        return columns.stream()
                .filter(column -> !EXCLUDED_TABLES.contains((String) column[0]))
                .toList();
    }

    private static String castType(String dataType) {
        return switch (dataType) {
            case "jsonb" -> "jsonb";
            case "json" -> "json";
            case "character varying" -> "varchar";
            default -> "text";
        };
    }

    static String escapeLike(String value) {
        return value.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    static String quote(String identifier) {
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }
}

package club.ttg.dnd5.dto.base.filters;

import club.ttg.dnd5.util.SwitchLayoutUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * Утилитные методы для построения QueryDSL предикатов.
 * Переиспользуемые во всех доменных PredicateBuilder.
 */
@UtilityClass
public class PredicateUtils
{
    /**
     * 3-state фильтр: IN / NOT IN для простых полей.
     */
    public <T> void applyThreeState(final BooleanBuilder builder,
                                     final ThreeStateFilter<T> filter,
                                     final SimpleExpression<T> path)
    {
        if (filter == null || !filter.isActive())
        {
            return;
        }

        if (!filter.getInclude().isEmpty())
        {
            builder.and(path.in(filter.getInclude()));
        }

        if (!filter.getExclude().isEmpty())
        {
            builder.and(path.notIn(filter.getExclude()));
        }
    }

    /**
     * 3-state фильтр для Enum: сравнение как String (Enum → name()).
     */
    public <E extends Enum<E>> void applyThreeStateEnum(final BooleanBuilder builder,
                                                         final ThreeStateFilter<E> filter,
                                                         final StringPath path)
    {
        if (filter == null || !filter.isActive())
        {
            return;
        }

        if (!filter.getInclude().isEmpty())
        {
            builder.and(path.in(
                    filter.getInclude().stream()
                            .map(Enum::name)
                            .toList()
            ));
        }

        if (!filter.getExclude().isEmpty())
        {
            builder.and(path.notIn(
                    filter.getExclude().stream()
                            .map(Enum::name)
                            .toList()
            ));
        }
    }

    /**
     * 3-state singleton: Boolean поле сущности.
     */
    public void applySingleton(final BooleanBuilder builder,
                                final ThreeStateSingleton singleton,
                                final BooleanPath path)
    {
        if (singleton == null || !singleton.isActive())
        {
            return;
        }

        builder.and(singleton.isPositive() ? path.isTrue() : path.isFalse());
    }

    /**
     * 3-state singleton с нативным SQL (для JSONB-полей и подзапросов).
     */
    public void applySingletonNative(final BooleanBuilder builder,
                                      final ThreeStateSingleton singleton,
                                      final String positiveSql,
                                      final String negativeSql)
    {
        if (singleton == null || !singleton.isActive())
        {
            return;
        }

        builder.and(Expressions.booleanTemplate(
                singleton.isPositive() ? positiveSql : negativeSql
        ));
    }

    /**
     * 3-state фильтр для JSONB-массивов Enum — через {@code jsonb_exists_any}.
     */
    public <E extends Enum<E>> void applyJsonbEnumArray(final BooleanBuilder builder,
                                                         final ThreeStateFilter<E> filter,
                                                         final String columnName)
    {
        if (filter == null || !filter.isActive())
        {
            return;
        }

        if (!filter.getInclude().isEmpty())
        {
            String values = filter.getInclude().stream()
                    .map(Enum::name)
                    .map(s -> "\"" + s + "\"")
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");

            builder.and(Expressions.booleanTemplate(
                    "jsonb_exists_any(" + columnName + ", array[" + values + "]::text[])"
            ));
        }

        if (!filter.getExclude().isEmpty())
        {
            String values = filter.getExclude().stream()
                    .map(Enum::name)
                    .map(s -> "\"" + s + "\"")
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");

            builder.and(Expressions.booleanTemplate(
                    "NOT jsonb_exists_any(" + columnName + ", array[" + values + "]::text[])"
            ));
        }
    }

    /**
     * Текстовый поиск (ILIKE с экранированием спецсимволов и переключением раскладки).
     */
    public BooleanExpression buildTextSearch(final String text,
                                              final StringPath... paths)
    {
        if (!StringUtils.hasText(text))
        {
            return null;
        }

        String safe = escapeLike(text.trim());
        String switched = SwitchLayoutUtils.switchLayout(safe);

        BooleanBuilder textBuilder = new BooleanBuilder();

        for (StringPath path : paths)
        {
            textBuilder.or(path.likeIgnoreCase("%" + safe + "%"));
            textBuilder.or(path.likeIgnoreCase("%" + switched + "%"));
        }

        return (BooleanExpression) textBuilder.getValue();
    }

    /**
     * 2-state фильтр источников: IN по акронимам.
     * Использует PathBuilder для совместимости с JPASQLQuery (не JPA-маппинг).
     *
     * @param entityAlias алиас сущности (например "spell", "creature")
     * @param columnName  имя колонки-FK источника (обычно "source")
     */
    public void applySources(final BooleanBuilder builder,
                              final Set<String> enabledSources,
                              final String entityAlias,
                              final String columnName)
    {
        if (enabledSources != null && !enabledSources.isEmpty())
        {
            com.querydsl.core.types.dsl.PathBuilder<Object> path =
                    new com.querydsl.core.types.dsl.PathBuilder<>(Object.class, entityAlias);
            builder.and(path.getString(columnName).in(enabledSources));
        }
    }

    /**
     * @deprecated Используйте {@link #applySources(BooleanBuilder, Set, String, String)}.
     *             StringPath через JPA-ассоциацию (Q.source.acronym) несовместим с JPASQLQuery.
     */
    @Deprecated
    public void applySources(final BooleanBuilder builder,
                              final Set<String> enabledSources,
                              final StringPath sourcePath)
    {
        if (enabledSources != null && !enabledSources.isEmpty())
        {
            builder.and(sourcePath.in(enabledSources));
        }
    }

    /**
     * 3-state фильтр для JSONB-объекта с вложенным массивом enum:
     * {@code (column->'jsonKey') @> '["ENUM"]'::jsonb}.
     * Используется для структур вида {@code {"values": ["BEAST", "HUMANOID"]}}.
     *
     * @param columnName имя колонки JSONB
     * @param jsonKey    ключ внутри JSON-объекта, содержащий массив
     */
    public <E extends Enum<E>> void applyJsonbNestedEnumArray(final BooleanBuilder builder,
                                                               final ThreeStateFilter<E> filter,
                                                               final String columnName,
                                                               final String jsonKey)
    {
        if (filter == null || !filter.isActive())
        {
            return;
        }

        if (!filter.getInclude().isEmpty())
        {
            BooleanBuilder orBuilder = new BooleanBuilder();
            for (E val : filter.getInclude())
            {
                orBuilder.or(Expressions.booleanTemplate(
                        "(" + columnName + "->'" + jsonKey + "') @> '[\"" + val.name() + "\"]'::jsonb"
                ));
            }
            builder.and(orBuilder);
        }

        if (!filter.getExclude().isEmpty())
        {
            for (E val : filter.getExclude())
            {
                builder.and(Expressions.booleanTemplate(
                        "NOT ((" + columnName + "->'" + jsonKey + "') @> '[\"" + val.name() + "\"]'::jsonb)"
                ));
            }
        }
    }

    /**
     * 3-state фильтр для JSONB чувств (senses).
     * Проверяет наличие ненулевого значения: {@code (senses->>'key')::int > 0}
     * или булевого: {@code (senses->>'key') = 'true'}.
     *
     * @param senseKeys enum-ключи, toString().toLowerCase() даёт JSON-ключ
     */
    public <E extends Enum<E>> void applyJsonbSenseFilter(final BooleanBuilder builder,
                                                           final ThreeStateFilter<E> filter,
                                                           final String columnName)
    {
        if (filter == null || !filter.isActive())
        {
            return;
        }

        if (!filter.getInclude().isEmpty())
        {
            BooleanBuilder orBuilder = new BooleanBuilder();
            for (E val : filter.getInclude())
            {
                String key = val.name().toLowerCase();
                if ("unimpeded".equals(key))
                {
                    orBuilder.or(Expressions.booleanTemplate(
                            "(" + columnName + "->>'" + key + "') = 'true'"
                    ));
                }
                else
                {
                    orBuilder.or(Expressions.booleanTemplate(
                            "(" + columnName + "->>'" + key + "') ~ '^\\d+$' AND (" + columnName + "->>'" + key + "')::int > 0"
                    ));
                }
            }
            builder.and(orBuilder);
        }

        if (!filter.getExclude().isEmpty())
        {
            for (E val : filter.getExclude())
            {
                String key = val.name().toLowerCase();
                if ("unimpeded".equals(key))
                {
                    builder.and(Expressions.booleanTemplate(
                            "((" + columnName + "->>'" + key + "') IS NULL OR (" + columnName + "->>'" + key + "') != 'true')"
                    ));
                }
                else
                {
                    builder.and(Expressions.booleanTemplate(
                            "((" + columnName + "->>'" + key + "') IS NULL OR (" + columnName + "->>'" + key + "') !~ '^\\d+$' OR (" + columnName + "->>'" + key + "')::int <= 0)"
                    ));
                }
            }
        }
    }

    /**
     * 3-state фильтр для JSONB-массива объектов по строковому ключу:
     * {@code exists (select 1 from jsonb_array_elements(column) as elem where elem->>'name' = 'value')}.
     * Используется для traits.
     */
    public void applyJsonbNamedArray(final BooleanBuilder builder,
                                      final ThreeStateFilter<String> filter,
                                      final String columnName,
                                      final String jsonFieldName)
    {
        if (filter == null || !filter.isActive())
        {
            return;
        }

        if (!filter.getInclude().isEmpty())
        {
            String condition = filter.getInclude().stream()
                    .map(v -> "elem->>'" + jsonFieldName + "' = '" + escapeSql(v) + "'")
                    .reduce((a, b) -> a + " or " + b)
                    .orElse("false");

            builder.and(Expressions.booleanTemplate(
                    "(" + columnName + " is not null and exists (select 1 from jsonb_array_elements(" + columnName + ") as elem where " + condition + "))"
            ));
        }

        if (!filter.getExclude().isEmpty())
        {
            String condition = filter.getExclude().stream()
                    .map(v -> "elem->>'" + jsonFieldName + "' = '" + escapeSql(v) + "'")
                    .reduce((a, b) -> a + " or " + b)
                    .orElse("false");

            builder.and(Expressions.booleanTemplate(
                    "(" + columnName + " is not null and not exists (select 1 from jsonb_array_elements(" + columnName + ") as elem where " + condition + "))"
            ));
        }
    }

    /**
     * 3-state фильтр для тегов — ILIKE поиск по JSONB-текстовому полю + имени сущности.
     * {@code COALESCE(column->>'text', '') ILIKE '%tag%'}
     */
    public void applyJsonbTagFilter(final BooleanBuilder builder,
                                     final ThreeStateFilter<String> filter,
                                     final String jsonColumnName,
                                     final StringPath namePath)
    {
        if (filter == null || !filter.isActive())
        {
            return;
        }

        if (!filter.getInclude().isEmpty())
        {
            BooleanBuilder orBuilder = new BooleanBuilder();
            for (String val : filter.getInclude())
            {
                String safe = escapeSql(val);
                orBuilder.or(Expressions.booleanTemplate(
                        "(COALESCE(" + jsonColumnName + "->>'text', '') ILIKE '%" + safe + "%')"
                ));
                orBuilder.or(namePath.containsIgnoreCase(val));
            }
            builder.and(orBuilder);
        }

        if (!filter.getExclude().isEmpty())
        {
            for (String val : filter.getExclude())
            {
                String safe = escapeSql(val);
                BooleanExpression typeNotContains = Expressions.booleanTemplate(
                        "NOT (COALESCE(" + jsonColumnName + "->>'text', '') ILIKE '%" + safe + "%')"
                );
                builder.and(typeNotContains.and(namePath.containsIgnoreCase(val).not()));
            }
        }
    }

    /**
     * Экранирование спецсимволов SQL LIKE (%, _, \).
     */
    private String escapeLike(final String input)
    {
        return input
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    /**
     * Экранирование SQL-строки (одинарные кавычки).
     */
    private String escapeSql(final String input)
    {
        return input.replace("'", "''").replace("\"", "\\\"");
    }
}

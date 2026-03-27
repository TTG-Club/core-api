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
                    .collect(java.util.stream.Collectors.joining(","));

            builder.and(Expressions.booleanTemplate(
                    "jsonb_exists_any(" + columnName + ", array[" + values + "]::text[])"
            ));
        }

        if (!filter.getExclude().isEmpty())
        {
            String values = filter.getExclude().stream()
                    .map(Enum::name)
                    .map(s -> "\"" + s + "\"")
                    .collect(java.util.stream.Collectors.joining(","));

            builder.and(Expressions.booleanTemplate(
                    "NOT jsonb_exists_any(" + columnName + ", array[" + values + "]::text[])"
            ));
        }
    }

    /**
     * Текстовый поиск (ILIKE с экранированием спецсимволов и переключением раскладки).
     */
    public com.querydsl.core.types.Predicate buildTextSearch(final String text,
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

        return textBuilder.getValue();
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
            @SuppressWarnings({"unchecked", "rawtypes"})
            com.querydsl.core.types.dsl.PathBuilder<Object> path =
                    new com.querydsl.core.types.dsl.PathBuilder(Object.class, entityAlias);
            builder.and(path.getString(columnName).in(enabledSources));
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
                    .collect(java.util.stream.Collectors.joining(" or "));

            builder.and(Expressions.booleanTemplate(
                    "(" + columnName + " is not null and exists (select 1 from jsonb_array_elements(" + columnName + ") as elem where " + condition + "))"
            ));
        }

        if (!filter.getExclude().isEmpty())
        {
            String condition = filter.getExclude().stream()
                    .map(v -> "elem->>'" + jsonFieldName + "' = '" + escapeSql(v) + "'")
                    .collect(java.util.stream.Collectors.joining(" or "));

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

    // ======================== QueryFilter-based methods ========================

    /**
     * Простой фильтр: IN / NOT IN для {@link QueryFilter}.
     */
    public <T> void applyFilter(final BooleanBuilder builder,
                                 final QueryFilter<T> filter,
                                 final SimpleExpression<T> path)
    {
        if (filter == null || !filter.isActive())
        {
            return;
        }

        if (filter.isExclude())
        {
            builder.and(path.notIn(filter.getValues()));
        }
        else if (filter.isUnion())
        {
            BooleanBuilder orBuilder = new BooleanBuilder();
            for (T val : filter.getValues())
            {
                orBuilder.or(path.eq(val));
            }
            builder.and(orBuilder);
        }
        else
        {
            builder.and(path.in(filter.getValues()));
        }
    }

    /**
     * Enum фильтр для {@link QueryFilter}: сравнение как String (Enum → name()).
     */
    public <E extends Enum<E>> void applyFilterEnum(final BooleanBuilder builder,
                                                      final QueryFilter<E> filter,
                                                      final StringPath path)
    {
        if (filter == null || !filter.isActive())
        {
            return;
        }

        java.util.List<String> names = filter.getValues().stream()
                .map(Enum::name)
                .toList();

        if (filter.isExclude())
        {
            builder.and(path.notIn(names));
        }
        else if (filter.isUnion())
        {
            BooleanBuilder orBuilder = new BooleanBuilder();
            for (String name : names)
            {
                orBuilder.or(path.eq(name));
            }
            builder.and(orBuilder);
        }
        else
        {
            builder.and(path.in(names));
        }
    }

    /**
     * Singleton фильтр для {@link QuerySingleton} с нативным SQL.
     */
    public void applySingletonFilter(final BooleanBuilder builder,
                                      final QuerySingleton singleton,
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
     * Singleton фильтр для {@link QuerySingleton} по boolean-полю.
     */
    public void applySingletonFilter(final BooleanBuilder builder,
                                      final QuerySingleton singleton,
                                      final BooleanPath path)
    {
        if (singleton == null || !singleton.isActive())
        {
            return;
        }

        builder.and(singleton.isPositive() ? path.isTrue() : path.isFalse());
    }

    /**
     * JSONB enum-массив для {@link QueryFilter}: {@code jsonb_exists_any}.
     */
    public <E extends Enum<E>> void applyJsonbEnumArrayFilter(final BooleanBuilder builder,
                                                                final QueryFilter<E> filter,
                                                                final String columnName)
    {
        if (filter == null || !filter.isActive())
        {
            return;
        }

        String values = filter.getValues().stream()
                .map(Enum::name)
                .map(s -> "\"" + s + "\"")
                .collect(java.util.stream.Collectors.joining(","));

        if (filter.isExclude())
        {
            builder.and(Expressions.booleanTemplate(
                    "NOT jsonb_exists_any(" + columnName + ", array[" + values + "]::text[])"
            ));
        }
        else
        {
            builder.and(Expressions.booleanTemplate(
                    "jsonb_exists_any(" + columnName + ", array[" + values + "]::text[])"
            ));
        }
    }

    /**
     * JSONB вложенный enum-массив для {@link QueryFilter}:
     * {@code (column->'jsonKey') @> '["ENUM"]'::jsonb}.
     */
    public <E extends Enum<E>> void applyJsonbNestedEnumArrayFilter(final BooleanBuilder builder,
                                                                      final QueryFilter<E> filter,
                                                                      final String columnName,
                                                                      final String jsonKey)
    {
        if (filter == null || !filter.isActive())
        {
            return;
        }

        if (filter.isExclude())
        {
            for (E val : filter.getValues())
            {
                builder.and(Expressions.booleanTemplate(
                        "NOT ((" + columnName + "->'" + jsonKey + "') @> '[\"" + val.name() + "\"]'::jsonb)"
                ));
            }
        }
        else if (filter.isUnion())
        {
            BooleanBuilder orBuilder = new BooleanBuilder();
            for (E val : filter.getValues())
            {
                orBuilder.or(Expressions.booleanTemplate(
                        "(" + columnName + "->'" + jsonKey + "') @> '[\"" + val.name() + "\"]'::jsonb"
                ));
            }
            builder.and(orBuilder);
        }
        else
        {
            for (E val : filter.getValues())
            {
                builder.and(Expressions.booleanTemplate(
                        "(" + columnName + "->'" + jsonKey + "') @> '[\"" + val.name() + "\"]'::jsonb"
                ));
            }
        }
    }

    /**
     * JSONB senses фильтр для {@link QueryFilter}.
     */
    public <E extends Enum<E>> void applyJsonbSenseFilterQuery(final BooleanBuilder builder,
                                                                 final QueryFilter<E> filter,
                                                                 final String columnName)
    {
        if (filter == null || !filter.isActive())
        {
            return;
        }

        for (E val : filter.getValues())
        {
            String key = val.name().toLowerCase();
            String existsSql;
            String notExistsSql;

            if ("unimpeded".equals(key))
            {
                existsSql = "(" + columnName + "->>'" + key + "') = 'true'";
                notExistsSql = "((" + columnName + "->>'" + key + "') IS NULL OR (" + columnName + "->>'" + key + "') != 'true')";
            }
            else
            {
                existsSql = "(" + columnName + "->>'" + key + "') ~ '^\\d+$' AND (" + columnName + "->>'" + key + "')::int > 0";
                notExistsSql = "((" + columnName + "->>'" + key + "') IS NULL OR (" + columnName + "->>'" + key + "') !~ '^\\d+$' OR (" + columnName + "->>'" + key + "')::int <= 0)";
            }

            if (filter.isExclude())
            {
                builder.and(Expressions.booleanTemplate(notExistsSql));
            }
            else if (filter.isUnion())
            {
                BooleanBuilder orBuilder = new BooleanBuilder();
                orBuilder.or(Expressions.booleanTemplate(existsSql));
                builder.and(orBuilder);
            }
            else
            {
                builder.and(Expressions.booleanTemplate(existsSql));
            }
        }
    }

    /**
     * JSONB named array фильтр для {@link QueryFilter} (traits по хэшам).
     * Хэши резолвятся в оригинальные значения через resolvedValues.
     */
    public void applyJsonbNamedArrayFilter(final BooleanBuilder builder,
                                             final QueryFilter<String> filter,
                                             final String columnName,
                                             final String jsonFieldName,
                                             final java.util.Collection<String> resolvedValues)
    {
        if (filter == null || !filter.isActive() || resolvedValues == null || resolvedValues.isEmpty())
        {
            return;
        }

        if (filter.isExclude())
        {
            String condition = resolvedValues.stream()
                    .map(v -> "elem->>'" + jsonFieldName + "' = '" + escapeSql(v) + "'")
                    .collect(java.util.stream.Collectors.joining(" or "));

            builder.and(Expressions.booleanTemplate(
                    "(" + columnName + " is not null and not exists (select 1 from jsonb_array_elements(" + columnName + ") as elem where " + condition + "))"
            ));
        }
        else if (filter.isUnion())
        {
            String condition = resolvedValues.stream()
                    .map(v -> "elem->>'" + jsonFieldName + "' = '" + escapeSql(v) + "'")
                    .collect(java.util.stream.Collectors.joining(" or "));

            builder.and(Expressions.booleanTemplate(
                    "(" + columnName + " is not null and exists (select 1 from jsonb_array_elements(" + columnName + ") as elem where " + condition + "))"
            ));
        }
        else
        {
            for (String val : resolvedValues)
            {
                builder.and(Expressions.booleanTemplate(
                        "(" + columnName + " is not null and exists (select 1 from jsonb_array_elements(" + columnName + ") as elem where elem->>'" + jsonFieldName + "' = '" + escapeSql(val) + "'))"
                ));
            }
        }
    }

    /**
     * JSONB tag фильтр для {@link QueryFilter} (tags по хэшам).
     * Хэши резолвятся в оригинальные значения через resolvedValues.
     */
    public void applyJsonbTagFilterQuery(final BooleanBuilder builder,
                                           final QueryFilter<String> filter,
                                           final String jsonColumnName,
                                           final StringPath namePath,
                                           final java.util.Collection<String> resolvedValues)
    {
        if (filter == null || !filter.isActive() || resolvedValues == null || resolvedValues.isEmpty())
        {
            return;
        }

        if (filter.isExclude())
        {
            for (String val : resolvedValues)
            {
                String safe = escapeSql(val);
                BooleanExpression typeNotContains = Expressions.booleanTemplate(
                        "NOT (COALESCE(" + jsonColumnName + "->>'text', '') ILIKE '%" + safe + "%')"
                );
                builder.and(typeNotContains.and(namePath.containsIgnoreCase(val).not()));
            }
        }
        else if (filter.isUnion())
        {
            BooleanBuilder orBuilder = new BooleanBuilder();
            for (String val : resolvedValues)
            {
                String safe = escapeSql(val);
                orBuilder.or(Expressions.booleanTemplate(
                        "(COALESCE(" + jsonColumnName + "->>'text', '') ILIKE '%" + safe + "%')"
                ));
                orBuilder.or(namePath.containsIgnoreCase(val));
            }
            builder.and(orBuilder);
        }
        else
        {
            for (String val : resolvedValues)
            {
                String safe = escapeSql(val);
                BooleanBuilder andBuilder = new BooleanBuilder();
                andBuilder.or(Expressions.booleanTemplate(
                        "(COALESCE(" + jsonColumnName + "->>'text', '') ILIKE '%" + safe + "%')"
                ));
                andBuilder.or(namePath.containsIgnoreCase(val));
                builder.and(andBuilder);
            }
        }
    }

    /**
     * 2-state фильтр источников для {@link QueryFilter}: IN по акронимам.
     */
    public void applySourcesFilter(final BooleanBuilder builder,
                                     final Set<String> enabledSources,
                                     final String entityAlias,
                                     final String columnName)
    {
        if (enabledSources != null && !enabledSources.isEmpty())
        {
            @SuppressWarnings({"unchecked", "rawtypes"})
            com.querydsl.core.types.dsl.PathBuilder<Object> path =
                    new com.querydsl.core.types.dsl.PathBuilder(Object.class, entityAlias);
            builder.and(path.getString(columnName).in(enabledSources));
        }
    }

    /**
     * Фильтр по JSONB-массиву временных объектов {value, unit}.
     * <p>
     * id формата {@code "UNIT"} (только unit, без числа: "ACTION", "BONUS") или
     * {@code "VALUE_UNIT"} (число + unit: "10_MINUTE", "1_HOUR").
     *
     * @param columnName имя JSONB-колонки (например, "casting_time" или "duration")
     */
    public void applyJsonbTimeFilter(final BooleanBuilder builder,
                                      final QueryFilter<String> filter,
                                      final String columnName)
    {
        if (filter == null || !filter.isActive())
        {
            return;
        }

        BooleanBuilder inner = new BooleanBuilder();
        for (String id : filter.getValues())
        {
            String sql = buildTimePredicate(id, columnName);
            if (sql != null)
            {
                inner.or(Expressions.booleanTemplate(sql));
            }
        }

        if (filter.isExclude())
        {
            builder.and(inner.not());
        }
        else
        {
            builder.and(inner);
        }
    }

    /**
     * Строит SQL-предикат для одного значения time-фильтра.
     *
     * @param id     формат "UNIT" или "VALUE_UNIT"
     * @param column имя JSONB-колонки
     */
    private String buildTimePredicate(final String id, final String column)
    {
        String[] parts = id.split("_", 2);

        if (parts.length == 1)
        {
            // Только unit: "ACTION", "BONUS", "INSTANT"
            return "exists (select 1 from jsonb_array_elements(%s) as elem where (elem->>'unit') = '%s')"
                    .formatted(column, escapeSql(parts[0]));
        }
        else
        {
            // value + unit: "10_MINUTE", "1_HOUR"
            try
            {
                Long.parseLong(parts[0]);
            }
            catch (NumberFormatException e)
            {
                return null;
            }

            return "exists (select 1 from jsonb_array_elements(%s) as elem where (elem->>'unit') = '%s' AND (elem->>'value')::bigint = %s)"
                    .formatted(column, escapeSql(parts[1]), parts[0]);
        }
    }
}


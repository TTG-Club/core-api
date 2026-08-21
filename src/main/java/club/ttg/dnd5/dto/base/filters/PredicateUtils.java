package club.ttg.dnd5.dto.base.filters;

import club.ttg.dnd5.util.SwitchLayoutUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * Утилитные методы для построения QueryDSL предикатов.
 * Переиспользуемые во всех доменных PredicateBuilder.
 * <p>
 * <b>Режим «исключить».</b> Исключающий предикат строится как положительное условие,
 * обёрнутое в {@code IS NOT TRUE} (для колонок — {@code notIn(...).or(path.isNull())}).
 * Голое {@code NOT ...} / {@code NOT IN ...} здесь не годится: когда поля у записи нет,
 * JSON-путь даёт SQL NULL, отрицание NULL — снова NULL, а {@code WHERE} пропускает
 * только TRUE. Из-за этого «исключить спас на Силу» выбрасывало заодно и все
 * заклинания, у которых спасбросков нет вовсе, а «исключить рубящий урон» — верёвку
 * и прочие непредметы. Запись без поля под исключение не подпадает и остаётся в выдаче.
 */
@UtilityClass
public class PredicateUtils
{
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
     * По умолчанию — ИЛИ (IN), при union=true — И (каждое значение должно совпасть).
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
            builder.and(path.notIn(filter.getValues()).or(path.isNull()));
        }
        else if (filter.isUnion())
        {
            for (T val : filter.getValues())
            {
                builder.and(path.eq(val));
            }
        }
        else
        {
            builder.and(path.in(filter.getValues()));
        }
    }

    /**
     * Строковый фильтр: IN / NOT IN / union для {@link QueryFilter} с {@link StringPath}.
     * Используется для полей типа String (например, srdVersion).
     */
    public void applyStringFilter(final BooleanBuilder builder,
                                   final QueryFilter<String> filter,
                                   final StringPath path)
    {
        if (filter == null || !filter.isActive())
        {
            return;
        }

        if (filter.isExclude())
        {
            builder.and(path.notIn(filter.getValues()).or(path.isNull()));
        }
        else if (filter.isUnion())
        {
            for (String val : filter.getValues())
            {
                builder.and(path.eq(val));
            }
        }
        else
        {
            builder.and(path.in(filter.getValues()));
        }
    }

    /**
     * Enum фильтр для {@link QueryFilter}: сравнение как String (Enum → name()).
     * По умолчанию — ИЛИ (IN), при union=true — И.
     */
    public <E extends Enum<E>> void applyFilterEnum(
            final BooleanBuilder builder,
            final QueryFilter<?> filter,
            final StringPath path,
            final Class<E> enumClass)
    {
        if (filter == null || !filter.isActive())
        {
            return;
        }

        List<String> names = filter.getValues().stream()
                .map(value -> toEnum(value, enumClass).name())
                .toList();

        if (filter.isExclude())
        {
            builder.and(path.notIn(names).or(path.isNull()));
        }
        else if (filter.isUnion())
        {
            for (String name : names)
            {
                builder.and(path.eq(name));
            }
        }
        else
        {
            builder.and(path.in(names));
        }
    }

    private <E extends Enum<E>> E toEnum(final Object value, final Class<E> enumClass)
    {
        if (value instanceof String str)
        {
            return Enum.valueOf(enumClass, str);
        }

        if (enumClass.isInstance(value))
        {
            return enumClass.cast(value);
        }

        throw new IllegalArgumentException(
                "Unsupported enum filter value type: " + value + ", class: "
                        + (value == null ? "null" : value.getClass().getName())
        );
    }

    /**
     * JSONB enum-массив для {@link QueryFilter}: {@code jsonb_exists_any}.
     * По умолчанию — ИЛИ (jsonb_exists_any), при union=true — И (jsonb_exists для каждого).
     */
    public <E extends Enum<E>> void applyJsonbEnumArrayFilter(final BooleanBuilder builder,
                                                                final QueryFilter<E> filter,
                                                                final String columnName)
    {
        if (filter == null || !filter.isActive())
        {
            return;
        }

        if (filter.isExclude())
        {
            String values = filter.getValues().stream()
                    .map(Enum::name)
                    .map(s -> "'" + s + "'")
                    .collect(java.util.stream.Collectors.joining(","));
            builder.and(Expressions.booleanTemplate(
                    "(jsonb_exists_any(" + columnName + ", array[" + values + "]::text[]) IS NOT TRUE)"
            ));
        }
        else if (filter.isUnion())
        {
            for (E val : filter.getValues())
            {
                builder.and(Expressions.booleanTemplate(
                        "jsonb_exists(" + columnName + ", '" + val.name() + "')"
                ));
            }
        }
        else
        {
            String values = filter.getValues().stream()
                    .map(Enum::name)
                    .map(s -> "'" + s + "'")
                    .collect(java.util.stream.Collectors.joining(","));
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
                        "(((" + columnName + "->'" + jsonKey + "') @> '[\"" + val.name() + "\"]'::jsonb) IS NOT TRUE)"
                ));
            }
        }
        else if (filter.isUnion())
        {
            for (E val : filter.getValues())
            {
                builder.and(Expressions.booleanTemplate(
                        "(" + columnName + "->'" + jsonKey + "') @> '[\"" + val.name() + "\"]'::jsonb"
                ));
            }
        }
        else
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

        if (filter.isExclude())
        {
            for (E val : filter.getValues())
            {
                builder.and(Expressions.booleanTemplate(buildSenseNotExistsSql(val, columnName)));
            }
        }
        else if (filter.isUnion())
        {
            for (E val : filter.getValues())
            {
                builder.and(Expressions.booleanTemplate(buildSenseExistsSql(val, columnName)));
            }
        }
        else
        {
            BooleanBuilder orBuilder = new BooleanBuilder();
            for (E val : filter.getValues())
            {
                orBuilder.or(Expressions.booleanTemplate(buildSenseExistsSql(val, columnName)));
            }
            builder.and(orBuilder);
        }
    }

    private <E extends Enum<E>> String buildSenseExistsSql(E val, String columnName)
    {
        String key = val.name().toLowerCase();
        if ("unimpeded".equals(key))
        {
            return "(" + columnName + "->>'" + key + "') = 'true'";
        }
        return "(" + columnName + "->>'" + key + "') ~ '^\\d+$' AND (" + columnName + "->>'" + key + "')::int > 0";
    }

    private <E extends Enum<E>> String buildSenseNotExistsSql(E val, String columnName)
    {
        String key = val.name().toLowerCase();
        if ("unimpeded".equals(key))
        {
            return "((" + columnName + "->>'" + key + "') IS NULL OR (" + columnName + "->>'" + key + "') != 'true')";
        }
        return "((" + columnName + "->>'" + key + "') IS NULL OR (" + columnName + "->>'" + key + "') !~ '^\\d+$' OR (" + columnName + "->>'" + key + "')::int <= 0)";
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
                    "(not exists (select 1 from jsonb_array_elements(coalesce(" + columnName + ", '[]'::jsonb)) as elem where " + condition + "))"
            ));
        }
        else if (filter.isUnion())
        {
            for (String val : resolvedValues)
            {
                builder.and(Expressions.booleanTemplate(
                        "(" + columnName + " is not null and exists (select 1 from jsonb_array_elements(" + columnName + ") as elem where elem->>'" + jsonFieldName + "' = '" + escapeSql(val) + "'))"
                ));
            }
        }
        else
        {
            String condition = resolvedValues.stream()
                    .map(v -> "elem->>'" + jsonFieldName + "' = '" + escapeSql(v) + "'")
                    .collect(java.util.stream.Collectors.joining(" or "));

            builder.and(Expressions.booleanTemplate(
                    "(" + columnName + " is not null and exists (select 1 from jsonb_array_elements(" + columnName + ") as elem where " + condition + "))"
            ));
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
        else
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

    public void applyJsonbMeasureFilter(final BooleanBuilder builder,
                                         final QueryFilter<String> filter,
                                         final String columnName)
    {
        applyJsonbTimeFilter(builder, filter, columnName);
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

    /**
     * Фильтр по строковому полю внутри вложенного JSONB-объекта.
     * Пример: {@code (weapon->'damage'->>'type') IN ('SLASHING','PIERCING')}.
     * <p>
     * Записи без такого объекта (у предмета нет оружейной части) не проходят включающий
     * фильтр, но проходят исключающий: верёвка не рубящее оружие, а значит не должна
     * пропадать из списка при исключении рубящего урона. См. замечание о режиме «исключить»
     * в javadoc класса.
     *
     * @param columnName имя JSONB-колонки (например, "weapon")
     * @param objectKey  ключ вложенного объекта (например, "damage")
     * @param jsonKey    ключ поля внутри объекта (например, "type")
     */
    public <E extends Enum<E>> void applyJsonbNestedObjectEnumFieldFilter(final BooleanBuilder builder,
                                                                           final QueryFilter<E> filter,
                                                                           final String columnName,
                                                                           final String objectKey,
                                                                           final String jsonKey)
    {
        if (filter == null || !filter.isActive())
        {
            return;
        }

        String path = "(" + columnName + "->'" + objectKey + "'->>'" + jsonKey + "')";

        if (filter.isExclude())
        {
            builder.and(Expressions.booleanTemplate("((" + path + " IN (" + toQuotedNames(filter) + ")) IS NOT TRUE)"));
        }
        else if (filter.isUnion())
        {
            for (E val : filter.getValues())
            {
                builder.and(Expressions.booleanTemplate(path + " = '" + val.name() + "'"));
            }
        }
        else
        {
            builder.and(Expressions.booleanTemplate(path + " IN (" + toQuotedNames(filter) + ")"));
        }
    }

    /**
     * Значения enum-фильтра в виде списка SQL-строк: {@code 'ACID','COLD'}.
     */
    private <E extends Enum<E>> String toQuotedNames(final QueryFilter<E> filter)
    {
        return filter.getValues().stream()
                .map(Enum::name)
                .map(name -> "'" + name + "'")
                .collect(java.util.stream.Collectors.joining(","));
    }

    /**
     * Фильтр по строковому полю внутри JSONB-объекта (не массива).
     * Пример: {@code area_of_effect->>'type' IN ('CONE','SPHERE')}.
     */
    public <E extends Enum<E>> void applyJsonbObjectEnumFieldFilter(final BooleanBuilder builder,
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
            String values = filter.getValues().stream()
                    .map(Enum::name)
                    .map(s -> "'" + s + "'")
                    .collect(java.util.stream.Collectors.joining(","));
            builder.and(Expressions.booleanTemplate(
                    "(((" + columnName + "->>'" + jsonKey + "') IN (" + values + ")) IS NOT TRUE)"
            ));
        }
        else if (filter.isUnion())
        {
            for (E val : filter.getValues())
            {
                builder.and(Expressions.booleanTemplate(
                        "(" + columnName + "->>'" + jsonKey + "') = '" + val.name() + "'"
                ));
            }
        }
        else
        {
            String values = filter.getValues().stream()
                    .map(Enum::name)
                    .map(s -> "'" + s + "'")
                    .collect(java.util.stream.Collectors.joining(","));
            builder.and(Expressions.booleanTemplate(
                    "(" + columnName + "->>'" + jsonKey + "') IN (" + values + ")"
            ));
        }
    }
}


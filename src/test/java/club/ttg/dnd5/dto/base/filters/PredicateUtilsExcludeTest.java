package club.ttg.dnd5.dto.base.filters;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Режим «исключить» не должен выбрасывать записи, у которых фильтруемого поля нет вовсе:
 * заклинание без спасбросков не подпадает под «исключить спас на Силу», а верёвка —
 * под «исключить рубящий урон».
 * <p>
 * Предикаты уходят в базу нативным SQL ({@code JPASQLQuery} + {@code PostgreSQLTemplates}),
 * поэтому тесты фиксируют форму SQL: отрицание обязано быть NULL-безопасным, а включающий
 * и объединяющий режимы — остаться прежними.
 */
class PredicateUtilsExcludeTest
{
    private static final StringPath STRING_PATH = Expressions.stringPath("srd_version");
    private static final NumberPath<Long> NUMBER_PATH = Expressions.numberPath(Long.class, "experience");

    private static <T> QueryFilter<T> exclude(final Set<T> values)
    {
        QueryFilter<T> filter = new QueryFilter<>();
        filter.setValues(values);
        filter.setExclude(true);
        return filter;
    }

    private static <T> QueryFilter<T> include(final Set<T> values)
    {
        QueryFilter<T> filter = new QueryFilter<>();
        filter.setValues(values);
        return filter;
    }

    private static <T> QueryFilter<T> union(final Set<T> values)
    {
        QueryFilter<T> filter = new QueryFilter<>();
        filter.setValues(values);
        filter.setUnion(true);
        return filter;
    }

    private static String sql(final BooleanBuilder builder)
    {
        return builder.getValue() == null ? "" : builder.getValue().toString();
    }

    @Nested
    @DisplayName("JSONB-фильтры в режиме «исключить» NULL-безопасны")
    class NullSafeExclude
    {
        @Test
        @DisplayName("вложенный enum-массив: спасброски заклинания")
        void nestedEnumArray()
        {
            BooleanBuilder builder = new BooleanBuilder();
            PredicateUtils.applyJsonbNestedEnumArrayFilter(
                    builder, exclude(Set.of(Ability.STRENGTH)), "effect", "savingThrows");

            String sql = sql(builder);
            assertTrue(sql.contains("IS NOT TRUE"), sql);
            assertFalse(sql.contains("NOT (("), sql);
        }

        @Test
        @DisplayName("enum-массив в колонке: характеристики предыстории")
        void enumArray()
        {
            BooleanBuilder builder = new BooleanBuilder();
            PredicateUtils.applyJsonbEnumArrayFilter(
                    builder, exclude(Set.of(Ability.STRENGTH)), "abilities");

            String sql = sql(builder);
            assertTrue(sql.contains("IS NOT TRUE"), sql);
            assertFalse(sql.contains("NOT jsonb_exists_any"), sql);
        }

        @Test
        @DisplayName("поле вложенного объекта: тип урона оружия")
        void nestedObjectField()
        {
            BooleanBuilder builder = new BooleanBuilder();
            PredicateUtils.applyJsonbNestedObjectEnumFieldFilter(
                    builder, exclude(Set.of(DamageType.SLASHING)), "weapon", "damage", "type");

            String sql = sql(builder);
            assertTrue(sql.contains("IS NOT TRUE"), sql);
            assertFalse(sql.contains("NOT IN"), sql);
        }

        @Test
        @DisplayName("поле объекта: тип атаки заклинания")
        void objectField()
        {
            BooleanBuilder builder = new BooleanBuilder();
            PredicateUtils.applyJsonbObjectEnumFieldFilter(
                    builder, exclude(Set.of(DamageType.SLASHING)), "effect", "attackType");

            String sql = sql(builder);
            assertTrue(sql.contains("IS NOT TRUE"), sql);
            assertFalse(sql.contains("NOT IN"), sql);
        }

        @Test
        @DisplayName("массив именованных объектов: особенности существа")
        void namedArray()
        {
            BooleanBuilder builder = new BooleanBuilder();
            PredicateUtils.applyJsonbNamedArrayFilter(
                    builder, exclude(Set.of("Чувство магии")), "traits", "name", Set.of("Чувство магии"));

            String sql = sql(builder);
            assertTrue(sql.contains("coalesce(traits"), sql);
            assertFalse(sql.contains("traits is not null"), sql);
        }
    }

    @Nested
    @DisplayName("Колоночные фильтры в режиме «исключить» пропускают NULL")
    class NullSafeColumnExclude
    {
        @Test
        @DisplayName("строковая колонка: версия SRD")
        void stringColumn()
        {
            BooleanBuilder builder = new BooleanBuilder();
            PredicateUtils.applyStringFilter(builder, exclude(Set.of("2024")), STRING_PATH);

            assertTrue(sql(builder).contains("srd_version is null"), sql(builder));
        }

        @Test
        @DisplayName("числовая колонка: опыт существа")
        void numberColumn()
        {
            BooleanBuilder builder = new BooleanBuilder();
            PredicateUtils.applyFilter(builder, exclude(Set.of(10L)), NUMBER_PATH);

            assertTrue(sql(builder).contains("experience is null"), sql(builder));
        }

        @Test
        @DisplayName("enum как строка: школа магии")
        void enumColumn()
        {
            BooleanBuilder builder = new BooleanBuilder();
            PredicateUtils.applyFilterEnum(
                    builder, exclude(Set.of(Ability.STRENGTH)), STRING_PATH, Ability.class);

            assertTrue(sql(builder).contains("srd_version is null"), sql(builder));
        }
    }

    @Nested
    @DisplayName("Включающий и объединяющий режимы не изменились")
    class PositiveModesUnchanged
    {
        @Test
        @DisplayName("включить: спасброски заклинания")
        void nestedEnumArrayInclude()
        {
            BooleanBuilder builder = new BooleanBuilder();
            PredicateUtils.applyJsonbNestedEnumArrayFilter(
                    builder, include(Set.of(Ability.STRENGTH)), "effect", "savingThrows");

            String sql = sql(builder);
            assertTrue(sql.contains("(effect->'savingThrows') @> '[\"STRENGTH\"]'::jsonb"), sql);
            assertFalse(sql.contains("IS NOT TRUE"), sql);
        }

        @Test
        @DisplayName("объединить: спасброски заклинания")
        void nestedEnumArrayUnion()
        {
            BooleanBuilder builder = new BooleanBuilder();
            PredicateUtils.applyJsonbNestedEnumArrayFilter(
                    builder, union(Set.of(Ability.STRENGTH, Ability.DEXTERITY)), "effect", "savingThrows");

            String sql = sql(builder);
            assertTrue(sql.contains("STRENGTH"), sql);
            assertTrue(sql.contains("DEXTERITY"), sql);
            assertFalse(sql.contains("IS NOT TRUE"), sql);
        }

        @Test
        @DisplayName("включить: тип урона оружия")
        void nestedObjectFieldInclude()
        {
            BooleanBuilder builder = new BooleanBuilder();
            PredicateUtils.applyJsonbNestedObjectEnumFieldFilter(
                    builder, include(Set.of(DamageType.SLASHING)), "weapon", "damage", "type");

            String sql = sql(builder);
            assertTrue(sql.contains("(weapon->'damage'->>'type') IN ('SLASHING')"), sql);
            assertFalse(sql.contains("IS NOT TRUE"), sql);
        }

        @Test
        @DisplayName("включить: версия SRD")
        void stringColumnInclude()
        {
            BooleanBuilder builder = new BooleanBuilder();
            PredicateUtils.applyStringFilter(builder, include(Set.of("2024")), STRING_PATH);

            assertFalse(sql(builder).contains("is null"), sql(builder));
        }
    }
}

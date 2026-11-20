package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureQueryRequest;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.dto.base.filters.PredicateSql;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * «Исключать» у логова и легендарного действия не отменяет остальные фильтры.
 * <p>
 * Условие исключения было шаблоном с {@code or} без скобок, и в SQL выходило
 * {@code … and types @> … and lair is null or … and source in (…)}: во второй ветке
 * {@code or} терялись поиск и тип — {@code type=DRAGON} вместе с {@code lair_mode=1}
 * отдавал существ всех типов.
 */
class CreaturePredicateBuilderTest
{
    private static QueryFilter<String> excluded()
    {
        QueryFilter<String> filter = new QueryFilter<>();
        filter.setValues(Set.of("1"));
        filter.setExclude(true);
        return filter;
    }

    /** Запрос, в котором есть фильтры и до условия исключения, и после него. */
    private static CreatureQueryRequest requestWithType()
    {
        QueryFilter<CreatureType> type = new QueryFilter<>();
        type.setValues(Set.of(CreatureType.DRAGON));

        CreatureQueryRequest request = new CreatureQueryRequest();
        request.setSearch("дракон");
        request.setType(type);
        request.setSource(Set.of("MM"));
        return request;
    }

    private static String sql(final CreatureQueryRequest request)
    {
        return PredicateSql.render(CreaturePredicateBuilder.build(request, List.of(), List.of()).getValue());
    }

    @Test
    @DisplayName("исключить логово: тип и поиск остаются в силе")
    void excludedLairKeepsOtherFilters()
    {
        CreatureQueryRequest request = requestWithType();
        request.setLair(excluded());

        PredicateSql.assertOrStaysInsideParentheses(sql(request));
    }

    @Test
    @DisplayName("исключить легендарное действие: тип и поиск остаются в силе")
    void excludedLegendaryActionKeepsOtherFilters()
    {
        CreatureQueryRequest request = requestWithType();
        request.setLegendaryAction(excluded());

        PredicateSql.assertOrStaysInsideParentheses(sql(request));
    }

    private static <E> QueryFilter<E> filter(final Set<E> values, final boolean exclude, final boolean union)
    {
        QueryFilter<E> filter = new QueryFilter<>();
        filter.setValues(values);
        filter.setExclude(exclude);
        filter.setUnion(union);
        return filter;
    }

    /** Ключ фильтра по типам урона → колонка таблицы bestiary. */
    private static final List<DamageFilter> DAMAGE_FILTERS = List.of(
            new DamageFilter("vulnerabilities", CreatureQueryRequest::setVulnerability),
            new DamageFilter("resistance", CreatureQueryRequest::setResistance),
            new DamageFilter("immunity_to_damage", CreatureQueryRequest::setDamageImmunity));

    private record DamageFilter(String column, BiConsumer<CreatureQueryRequest, QueryFilter<DamageType>> setter)
    {
    }

    @Test
    @DisplayName("уязвимость, сопротивление, иммунитет к урону: каждый ключ — в свою колонку")
    void damageFiltersIncludeByOwnColumn()
    {
        for (DamageFilter damage : DAMAGE_FILTERS)
        {
            CreatureQueryRequest request = new CreatureQueryRequest();
            damage.setter().accept(request, filter(Set.of(DamageType.FIRE), false, false));

            String sql = sql(request);
            assertTrue(sql.contains("jsonb_exists_any(" + damage.column() + ", array['FIRE']::text[])"), sql);
            DAMAGE_FILTERS.stream()
                    .filter(other -> other != damage)
                    .forEach(other -> assertFalse(sql.contains("(" + other.column() + ","), sql));
        }
    }

    @Test
    @DisplayName("иммунитет к состояниям — колонка immunity_to_condition")
    void conditionImmunityInclude()
    {
        CreatureQueryRequest request = new CreatureQueryRequest();
        request.setConditionImmunity(filter(Set.of(Condition.CHARMED), false, false));

        String sql = sql(request);
        assertTrue(sql.contains("jsonb_exists_any(immunity_to_condition, array['CHARMED']::text[])"), sql);
    }

    @Test
    @DisplayName("исключить уязвимость/сопротивление/иммунитет: NULL-безопасно, тип и поиск в силе")
    void excludedDamageAndConditionKeepOtherFilters()
    {
        CreatureQueryRequest request = requestWithType();
        request.setVulnerability(filter(Set.of(DamageType.FIRE), true, false));
        request.setResistance(filter(Set.of(DamageType.COLD), true, false));
        request.setDamageImmunity(filter(Set.of(DamageType.POISON), true, false));
        request.setConditionImmunity(filter(Set.of(Condition.POISONED), true, false));

        String sql = sql(request);
        PredicateSql.assertOrStaysInsideParentheses(sql);
        // IS NOT TRUE, а не NOT: у существа с пустой (null) колонкой условие остаётся истинным.
        for (String column : List.of("vulnerabilities", "resistance", "immunity_to_damage", "immunity_to_condition"))
        {
            assertTrue(sql.contains("(jsonb_exists_any(" + column + ", array["), sql);
        }
        assertFalse(sql.contains("NOT jsonb_exists"), sql);
        assertTrue(sql.contains("::text[]) IS NOT TRUE)"), sql);
    }

    @Test
    @DisplayName("совпадение всех: каждое значение — отдельным условием через and")
    void unionRequiresEveryValue()
    {
        CreatureQueryRequest request = new CreatureQueryRequest();
        request.setResistance(filter(Set.of(DamageType.FIRE, DamageType.COLD), false, true));
        request.setConditionImmunity(filter(Set.of(Condition.CHARMED, Condition.FRIGHTENED), false, true));

        String sql = sql(request);
        PredicateSql.assertOrStaysInsideParentheses(sql);
        assertTrue(sql.contains("jsonb_exists(resistance, 'FIRE')"), sql);
        assertTrue(sql.contains("jsonb_exists(resistance, 'COLD')"), sql);
        assertTrue(sql.contains("jsonb_exists(immunity_to_condition, 'CHARMED')"), sql);
        assertTrue(sql.contains("jsonb_exists(immunity_to_condition, 'FRIGHTENED')"), sql);
        assertFalse(sql.contains("jsonb_exists_any"), sql);
    }
}

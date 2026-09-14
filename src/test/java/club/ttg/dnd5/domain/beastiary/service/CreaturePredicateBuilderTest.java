package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureQueryRequest;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.dto.base.filters.PredicateSql;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

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
}

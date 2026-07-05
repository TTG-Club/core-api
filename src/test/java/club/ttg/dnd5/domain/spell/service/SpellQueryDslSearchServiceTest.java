package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.spell.rest.dto.SpellGrouping;
import club.ttg.dnd5.domain.spell.rest.dto.SpellSorting;
import com.querydsl.core.types.OrderSpecifier;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpellQueryDslSearchServiceTest
{
    private final SpellQueryDslSearchService service = new SpellQueryDslSearchService(null);

    @Test
    void schoolGroupingUsesPhysicalSpellColumn()
    {
        assertOrder(
                service.getOrder(SpellGrouping.SCHOOL, SpellSorting.NAME),
                "spell.school",
                "spell.name",
                "spell.url"
        );
    }

    @Test
    void levelGroupingPrecedesSelectedSorting()
    {
        assertOrder(
                service.getOrder(SpellGrouping.LEVEL, SpellSorting.ENGLISH),
                "spell.level",
                "spell.english",
                "spell.url"
        );
    }

    @Test
    void classGroupingPrecedesSelectedSorting()
    {
        assertOrder(
                service.getOrder(SpellGrouping.CLASS, SpellSorting.NAME),
                "coalesce((select min(sca.class_affiliation_url) from spell_class_affiliation sca where sca.spell_url = spell.url), '')",
                "spell.name",
                "spell.url"
        );
    }

    @Test
    void englishSortingKeepsStableUrlTieBreaker()
    {
        assertOrder(
                service.getOrder(SpellGrouping.NONE, SpellSorting.ENGLISH),
                "spell.english",
                "spell.url"
        );
    }

    @Test
    void levelSortingOrdersSpellsInsideSchoolGroups()
    {
        assertOrder(
                service.getOrder(SpellGrouping.SCHOOL, SpellSorting.LEVEL),
                "spell.school",
                "spell.level",
                "spell.name",
                "spell.url"
        );
    }

    @Test
    void levelSortingOrdersUngroupedSpells()
    {
        assertOrder(
                service.getOrder(SpellGrouping.NONE, SpellSorting.LEVEL),
                "spell.level",
                "spell.name",
                "spell.url"
        );
    }

    private void assertOrder(final OrderSpecifier<?>[] order, final String... expectedTargets)
    {
        assertEquals(
                Arrays.asList(expectedTargets),
                Arrays.stream(order).map(specifier -> specifier.getTarget().toString()).toList()
        );
    }
}

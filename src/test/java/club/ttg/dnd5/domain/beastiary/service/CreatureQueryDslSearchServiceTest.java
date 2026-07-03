package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureGrouping;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureSorting;
import com.querydsl.core.types.OrderSpecifier;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreatureQueryDslSearchServiceTest
{
    private final CreatureQueryDslSearchService service = new CreatureQueryDslSearchService(null);

    @Test
    void challengeRatingGroupingKeepsNameAndUrlTieBreakers()
    {
        assertOrder(
                service.getOrder(CreatureGrouping.CHALLENGE_RATING, CreatureSorting.NAME),
                "creature.experience",
                "creature.name",
                "creature.url"
        );
    }

    @Test
    void typeGroupingPrecedesEnglishSorting()
    {
        assertOrder(
                service.getOrder(CreatureGrouping.TYPE, CreatureSorting.ENGLISH),
                "cast(creature.types as text)",
                "creature.english",
                "creature.url"
        );
    }

    @Test
    void challengeRatingSortingStaysInsideTypeGroups()
    {
        assertOrder(
                service.getOrder(CreatureGrouping.TYPE, CreatureSorting.CHALLENGE_RATING),
                "cast(creature.types as text)",
                "creature.experience",
                "creature.name",
                "creature.url"
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

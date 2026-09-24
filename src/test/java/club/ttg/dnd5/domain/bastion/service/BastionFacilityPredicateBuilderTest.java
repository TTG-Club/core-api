package club.ttg.dnd5.domain.bastion.service;

import club.ttg.dnd5.domain.bastion.model.BastionOrder;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionFacilityQueryRequest;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BastionFacilityPredicateBuilderTest {

    @Test
    void sourcesNarrowResults() {
        BastionFacilityQueryRequest request = new BastionFacilityQueryRequest();
        request.setSource(Set.of("DMG24"));

        String predicate = BastionFacilityPredicateBuilder.build(request).toString();

        assertTrue(predicate.contains("bastion_facility.source"));
        assertTrue(predicate.contains("DMG24"));
    }

    @Test
    void orderFilterLooksIntoJsonbArray() {
        BastionFacilityQueryRequest request = new BastionFacilityQueryRequest();
        QueryFilter<BastionOrder> order = new QueryFilter<>();
        order.setValues(Set.of(BastionOrder.CRAFT));
        request.setOrder(order);

        String predicate = BastionFacilityPredicateBuilder.build(request).toString();

        assertTrue(predicate.contains("orders"));
        assertTrue(predicate.contains("CRAFT"));
    }

    @Test
    void prerequisiteFilterSplitsByPresence() {
        BastionFacilityQueryRequest withRequirement = new BastionFacilityQueryRequest();
        QueryFilter<String> has = new QueryFilter<>();
        has.setValues(Set.of("1"));
        withRequirement.setPrerequisite(has);

        BastionFacilityQueryRequest withoutRequirement = new BastionFacilityQueryRequest();
        QueryFilter<String> hasNot = new QueryFilter<>();
        hasNot.setValues(Set.of("1"));
        hasNot.setExclude(true);
        withoutRequirement.setPrerequisite(hasNot);

        assertTrue(BastionFacilityPredicateBuilder.build(withRequirement).toString()
                .contains("prerequisite is not null"));
        assertTrue(BastionFacilityPredicateBuilder.build(withoutRequirement).toString()
                .contains("prerequisite is null"));
    }

    @Test
    void emptyRequestShowsOnlyVisible() {
        String predicate = BastionFacilityPredicateBuilder.build(new BastionFacilityQueryRequest()).toString();

        assertTrue(predicate.contains("isHiddenEntity = false"));
        assertFalse(predicate.contains("bastion_facility.source"));
    }
}

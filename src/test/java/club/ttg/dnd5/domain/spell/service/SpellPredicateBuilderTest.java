package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.HealingType;
import club.ttg.dnd5.domain.spell.rest.dto.SpellQueryRequest;
import club.ttg.dnd5.dto.base.filters.PredicateSql;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import com.querydsl.core.BooleanBuilder;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SpellPredicateBuilderTest {

    @Test
    void classGroupRequiresExactClassAffiliation() {
        SpellQueryRequest request = new SpellQueryRequest();
        request.setClassGroup("wizard-phb");

        String predicate = SpellPredicateBuilder.build(request, Set.of(), Set.of()).toString();

        assertTrue(predicate.contains("spell_class_affiliation"));
        assertTrue(predicate.contains("class_affiliation_url"));
        assertTrue(predicate.contains("wizard-phb"));
    }

    @Test
    void withoutClassGroupRequiresMissingClassAffiliation() {
        SpellQueryRequest request = new SpellQueryRequest();
        request.setClassGroup(SpellQueryRequest.WITHOUT_CLASS_GROUP);

        String predicate = SpellPredicateBuilder.build(request, Set.of(), Set.of()).toString();

        assertTrue(predicate.contains("not exists"));
        assertTrue(predicate.contains("spell_class_affiliation"));
    }

    @Test
    void healingFilterChecksLegacyHealingTypesAndDamageFormulaMarkers() {
        SpellQueryRequest request = new SpellQueryRequest();
        QueryFilter<HealingType> filter = new QueryFilter<>();
        filter.setValues(Set.of(HealingType.HEALING));
        request.setHealingType(filter);

        String predicate = SpellPredicateBuilder.build(request, Set.of(), Set.of()).toString();

        assertTrue(predicate.contains("healingTypes"));
        assertTrue(predicate.contains("damageFormulas"));
        assertTrue(predicate.contains("@heal"));
    }

    @Test
    void excludedHealingFilterChecksLegacyHealingTypesAndDamageFormulaMarkers() {
        SpellQueryRequest request = new SpellQueryRequest();
        QueryFilter<HealingType> filter = new QueryFilter<>();
        filter.setValues(Set.of(HealingType.TEMPORARY_HITPOINTS));
        filter.setExclude(true);
        request.setHealingType(filter);

        String predicate = SpellPredicateBuilder.build(request, Set.of(), Set.of()).toString();

        assertTrue(predicate.contains("not exists"));
        assertTrue(predicate.contains("healingTypes"));
        assertTrue(predicate.contains("damageFormulas"));
        assertTrue(predicate.contains("heal.temp"));
    }

    @Test
    void damageTypeFilterChecksFilterFieldAndDamageFormulaMarkers() {
        SpellQueryRequest request = new SpellQueryRequest();
        request.setSearch("шарик");
        QueryFilter<DamageType> filter = new QueryFilter<>();
        filter.setValues(Set.of(DamageType.FIRE));
        request.setDamageType(filter);

        BooleanBuilder predicate = SpellPredicateBuilder.build(request, Set.of(), Set.of());
        String description = predicate.toString();

        assertTrue(description.contains("effect->'damageTypes'"));
        assertTrue(description.contains("effect->'damageFormulas'"));
        assertTrue(description.contains("FIRE"));
        assertTrue(description.contains("%@dmg.fire%"));
        // Совпадение «по полю или по формуле» не должно разрывать where: иначе поиск,
        // добавленный раньше, перестанет действовать.
        PredicateSql.assertOrStaysInsideParentheses(PredicateSql.render(predicate.getValue()));
    }

    @Test
    void excludedDamageTypeFilterRequiresNeitherFilterFieldNorDamageFormulaMarker() {
        SpellQueryRequest request = new SpellQueryRequest();
        QueryFilter<DamageType> filter = new QueryFilter<>();
        filter.setValues(Set.of(DamageType.COLD));
        filter.setExclude(true);
        request.setDamageType(filter);

        BooleanBuilder predicate = SpellPredicateBuilder.build(request, Set.of(), Set.of());
        String description = predicate.toString();

        assertTrue(description.contains("not exists (select 1 from jsonb_array_elements_text(coalesce(effect->'damageTypes'"));
        assertTrue(description.contains("and not exists (select 1 from jsonb_array_elements_text(coalesce(effect->'damageFormulas'"));
        assertTrue(description.contains("%@dmg.cold%"));
        PredicateSql.assertOrStaysInsideParentheses(PredicateSql.render(predicate.getValue()));
    }
}

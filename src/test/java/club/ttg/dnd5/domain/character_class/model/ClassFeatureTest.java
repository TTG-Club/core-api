package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.character_class.rest.dto.ClassFeatureDto;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassFeatureRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassFeatureTest {

    @Test
    void requestConstructorCopiesOptionsName() {
        ClassFeatureRequest request = new ClassFeatureRequest();
        request.setName("Combat Superiority");
        request.setOptionsName("Maneuvers");
        request.setFightingStyleChoice(true);

        ClassFeature feature = new ClassFeature(request);

        assertEquals("Maneuvers", feature.getOptionsName());
        assertTrue(feature.isFightingStyleChoice());
    }

    @Test
    void dtoConstructorCopiesOptionsName() {
        ClassFeature feature = new ClassFeature();
        feature.setName("Combat Superiority");
        feature.setOptionsName("Maneuvers");
        feature.setFightingStyleChoice(true);

        ClassFeatureDto dto = new ClassFeatureDto(feature, false);

        assertEquals("Maneuvers", dto.getOptionsName());
        assertTrue(dto.isFightingStyleChoice());
    }

    @Test
    void requestConstructorCopiesAbilityImprovement() {
        ClassFeatureRequest request = new ClassFeatureRequest();
        request.setName("Ability Score Improvement");
        request.setAbilityImprovement(true);

        ClassFeature feature = new ClassFeature(request);

        assertTrue(feature.isAbilityImprovement());
    }

    @Test
    void dtoConstructorCopiesAbilityImprovement() {
        ClassFeature feature = new ClassFeature();
        feature.setName("Ability Score Improvement");
        feature.setAbilityImprovement(true);

        ClassFeatureDto dto = new ClassFeatureDto(feature, false);

        assertTrue(dto.isAbilityImprovement());
    }
}

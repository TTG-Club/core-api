package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.character_class.rest.dto.ClassFeatureDto;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassFeatureRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassFeatureTest {

    @Test
    void requestConstructorCopiesOptionsName() {
        ClassFeatureRequest request = new ClassFeatureRequest();
        request.setName("Combat Superiority");
        request.setOptionsName("Maneuvers");

        ClassFeature feature = new ClassFeature(request);

        assertEquals("Maneuvers", feature.getOptionsName());
    }

    @Test
    void dtoConstructorCopiesOptionsName() {
        ClassFeature feature = new ClassFeature();
        feature.setName("Combat Superiority");
        feature.setOptionsName("Maneuvers");

        ClassFeatureDto dto = new ClassFeatureDto(feature, false);

        assertEquals("Maneuvers", dto.getOptionsName());
    }
}

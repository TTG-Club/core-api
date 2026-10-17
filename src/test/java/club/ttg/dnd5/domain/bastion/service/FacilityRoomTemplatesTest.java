package club.ttg.dnd5.domain.bastion.service;

import club.ttg.dnd5.domain.bastion.model.FacilityRoomTemplate;
import club.ttg.dnd5.domain.bastion.model.FacilityRoomTemplate.Item;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.player.plan.PlanDocument.FurnitureKind;
import club.ttg.dnd5.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FacilityRoomTemplatesTest {

    private static FacilityRoomTemplate roomy(Item... items) {
        return new FacilityRoomTemplate(FacilitySpace.ROOMY, 4, 4, List.of(items));
    }

    @Test
    void validTemplatePasses() {
        assertDoesNotThrow(() -> FacilityRoomTemplates.validate(List.of(
                roomy(new Item(FurnitureKind.BED, 0, 0, 0), new Item(FurnitureKind.TABLE, 2, 3, 0)),
                new FacilityRoomTemplate(FacilitySpace.CRAMPED, 2, 2, List.of()))));
        assertDoesNotThrow(() -> FacilityRoomTemplates.validate(null));
    }

    @Test
    void sizeMustMatchSpace() {
        assertThrows(ApiException.class, () -> FacilityRoomTemplates.validate(
                List.of(new FacilityRoomTemplate(FacilitySpace.ROOMY, 4, 3, List.of()))));
    }

    @Test
    void furnitureStaysInsideAndApart() {
        assertThrows(ApiException.class, () -> FacilityRoomTemplates.validate(
                List.of(roomy(new Item(FurnitureKind.TABLE, 3, 0, 0)))));
        assertDoesNotThrow(() -> FacilityRoomTemplates.validate(
                List.of(roomy(new Item(FurnitureKind.TABLE, 3, 0, 90)))));
        assertThrows(ApiException.class, () -> FacilityRoomTemplates.validate(
                List.of(roomy(new Item(FurnitureKind.TABLE, 0, 0, 0), new Item(FurnitureKind.CHAIR, 1, 0, 0)))));
    }

    @Test
    void oneTemplatePerSpace() {
        assertThrows(ApiException.class, () -> FacilityRoomTemplates.validate(List.of(roomy(), roomy())));
    }

    @Test
    void templateIsPickedBySpace() {
        FacilityRoomTemplate template = roomy();

        assertSame(template, FacilityRoomTemplates.forSpace(List.of(template), FacilitySpace.ROOMY));
        assertNull(FacilityRoomTemplates.forSpace(List.of(template), FacilitySpace.VAST));
        assertNull(FacilityRoomTemplates.forSpace(null, FacilitySpace.VAST));
    }
}

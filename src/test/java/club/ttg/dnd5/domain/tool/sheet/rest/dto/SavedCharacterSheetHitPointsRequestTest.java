package club.ttg.dnd5.domain.tool.sheet.rest.dto;

import club.ttg.dnd5.domain.tool.tracker.model.ParticipantType;
import club.ttg.dnd5.domain.tool.tracker.rest.dto.ParticipantAddRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Временные хиты в запросах трекера и листа: не прислано и 0 — можно, отрицательное — 400. */
class SavedCharacterSheetHitPointsRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void temporaryIsOptional() {
        assertTrue(validator.validate(new SavedCharacterSheetHitPointsRequest(17, null)).isEmpty());
        assertTrue(validator.validate(new SavedCharacterSheetHitPointsRequest(17, 0)).isEmpty());
        assertTrue(validator.validate(new SavedCharacterSheetHitPointsRequest(17, 5)).isEmpty());
    }

    @Test
    void negativeTemporaryIsRejected() {
        var violations = validator.validate(new SavedCharacterSheetHitPointsRequest(17, -1));

        assertEquals(1, violations.size());
        assertEquals("temporary", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void negativeParticipantTemporaryHitPointsIsRejected() {
        ParticipantAddRequest request = new ParticipantAddRequest();
        request.setType(ParticipantType.PLAYER);
        request.setName("Паладин");
        request.setTemporaryHitPoints(-1);

        var violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("temporaryHitPoints", violations.iterator().next().getPropertyPath().toString());
    }
}

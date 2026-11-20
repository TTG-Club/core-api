package club.ttg.dnd5.domain.tool.tracker.rest.dto;

import club.ttg.dnd5.domain.tool.tracker.model.ParticipantType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Временные хиты игрока при добавлении в трекер: отрицательное — 400. */
class ParticipantAddRequestTest {

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

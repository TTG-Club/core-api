package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.CreatureTrait;
import club.ttg.dnd5.domain.beastiary.rest.dto.TraitRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Круг «мастерская открыла черту и сохранила»: {@code /raw} отдаёт {@link TraitRequest},
 * сохранение переписывает JSONB целиком — поле, которого нет в запросе, пропало бы.
 */
class TraitMapperTest {
    private final TraitMapper mapper = new TraitMapperImpl();

    @Test
    void keepsSaveSuccessPerDayThroughFormRoundTrip() throws Exception {
        TraitRequest request = new ObjectMapper().readValue("""
                {"name": {"rus": "Легендарное сопротивление"}, "saveSuccessPerDay": 3}""",
                TraitRequest.class);

        CreatureTrait stored = mapper.toEntity(request);
        CreatureTrait afterRoundTrip = mapper.toEntity(mapper.toRequest(stored));

        assertEquals(3, stored.getSaveSuccessPerDay());
        assertEquals(3, afterRoundTrip.getSaveSuccessPerDay());
    }

    /** Старая черта без поля остаётся без него — и в JSONB пустое значение не пишется. */
    @Test
    void leavesSaveSuccessPerDayEmptyWhenNotSet() throws Exception {
        CreatureTrait stored = new CreatureTrait();
        stored.setName("Амфибия");

        CreatureTrait afterRoundTrip = mapper.toEntity(mapper.toRequest(stored));

        assertNull(afterRoundTrip.getSaveSuccessPerDay());
        assertFalse(new ObjectMapper().writeValueAsString(afterRoundTrip).contains("saveSuccessPerDay"));
    }
}

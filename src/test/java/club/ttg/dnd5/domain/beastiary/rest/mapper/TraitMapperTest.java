package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.CreatureTrait;
import club.ttg.dnd5.domain.beastiary.rest.dto.TraitRequest;
import club.ttg.dnd5.domain.common.dictionary.RechargeType;
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

    /** «Раз в N ходов» у черты переживает круг формы так же, как у действия. */
    @Test
    void keepsCooldownTurnsThroughFormRoundTrip() throws Exception {
        TraitRequest request = new ObjectMapper().readValue("""
                {"name": {"rus": "Морозная вспышка"}, "cooldownTurns": 4}""",
                TraitRequest.class);

        CreatureTrait stored = mapper.toEntity(request);
        CreatureTrait afterRoundTrip = mapper.toEntity(mapper.toRequest(stored));

        assertEquals(4, stored.getCooldownTurns());
        assertEquals(4, afterRoundTrip.getCooldownTurns());
    }

    /** Рядом с перезарядкой число ходов не хранится: система всё равно возьмёт перезарядку. */
    @Test
    void dropsCooldownTurnsNextToRecharge() {
        TraitRequest request = new TraitRequest();
        request.setRecharge(RechargeType.LR);
        request.setCooldownTurns(3);

        CreatureTrait stored = mapper.toEntity(request);

        assertEquals(RechargeType.LR, stored.getRecharge());
        assertNull(stored.getCooldownTurns());
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

    /** Черта без «раз в N ходов» остаётся без него — и в JSONB пустое значение не пишется. */
    @Test
    void leavesCooldownTurnsEmptyWhenNotSet() throws Exception {
        CreatureTrait stored = new CreatureTrait();
        stored.setName("Амфибия");

        CreatureTrait afterRoundTrip = mapper.toEntity(mapper.toRequest(stored));

        assertNull(afterRoundTrip.getCooldownTurns());
        assertFalse(new ObjectMapper().writeValueAsString(afterRoundTrip).contains("cooldownTurns"));
    }
}

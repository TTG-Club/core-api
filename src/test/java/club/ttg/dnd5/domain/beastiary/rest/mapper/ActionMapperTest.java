package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.action.AttackType;
import club.ttg.dnd5.domain.beastiary.model.action.CreatureAction;
import club.ttg.dnd5.domain.beastiary.model.action.CreatureActionEffect;
import club.ttg.dnd5.domain.beastiary.model.action.SawingThrow;
import club.ttg.dnd5.domain.beastiary.rest.dto.ActionRequest;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.RechargeType;
import club.ttg.dnd5.domain.common.model.DamagePart;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Круг «мастерская открыла запись и сохранила»: {@code /raw} отдаёт
 * {@link ActionRequest}, а сохранение переписывает JSONB целиком. Всё, что было
 * в записи до круга, обязано в ней и остаться — иначе заполненный каталог
 * обеднеет от одного захода в форму.
 */
class ActionMapperTest {
    private final ActionMapper mapper = new ActionMapperImpl();

    /** Запись старого импорта: механику не заводили, поля лежат по-старому. */
    @Test
    void keepsLegacyFieldsThroughFormRoundTrip() {
        CreatureAction stored = new CreatureAction();
        stored.setName("Укус");
        stored.setEnglish("Bite");
        stored.setDescription("[\"*Рукопашная атака:* +9 к попаданию.\"]");
        stored.setOriginal("Melee Attack Roll: +9, reach 10 ft.");
        stored.setRecharge(RechargeType.D5);
        stored.setAttackType(AttackType.MELEE);
        stored.setSawingThrows(List.of(savingThrow(Ability.CONSTITUTION, 14)));
        stored.setDamageTypes(List.of(DamageType.PIERCING));

        CreatureAction afterRoundTrip = mapper.toEntity(mapper.toRequest(stored));

        assertEquals("Укус", afterRoundTrip.getName());
        assertEquals("Bite", afterRoundTrip.getEnglish());
        assertEquals(stored.getDescription(), afterRoundTrip.getDescription());
        // Английский оригинал форма не показывает — и не должна его терять.
        assertEquals(stored.getOriginal(), afterRoundTrip.getOriginal());
        assertEquals(RechargeType.D5, afterRoundTrip.getRecharge());
        assertEquals(AttackType.MELEE, afterRoundTrip.getAttackType());
        assertEquals(1, afterRoundTrip.getSawingThrows().size());
        assertEquals(List.of(DamageType.PIERCING), afterRoundTrip.getDamageTypes());
    }

    /** Поля старого импорта поднимаются в механику, чтобы форма их показала. */
    @Test
    void raisesLegacyFieldsIntoEffect() {
        CreatureAction stored = new CreatureAction();
        stored.setName("Дыхание");
        stored.setAttackType(AttackType.RANGE);
        stored.setSawingThrows(List.of(savingThrow(Ability.DEXTERITY, 17)));
        stored.setDamageTypes(List.of(DamageType.FIRE));

        ActionRequest request = mapper.toRequest(stored);

        assertNotNull(request.getEffect());
        assertEquals(AttackType.RANGE, request.getEffect().getAttackType());
        assertEquals(17, request.getEffect().getSavingThrows().iterator().next().getDc());
        assertEquals(List.of(DamageType.FIRE), request.getEffect().getDamageTypes());
    }

    /** Записи без всякой механики механику и не выдумывают. */
    @Test
    void leavesEffectEmptyWithoutMechanics() {
        CreatureAction stored = new CreatureAction();
        stored.setName("Мультиатака");
        stored.setDescription("[\"Существо совершает две атаки.\"]");

        ActionRequest request = mapper.toRequest(stored);

        assertNull(request.getEffect());
        assertNull(mapper.toEntity(request).getAttackType());
    }

    /** Заведённая в форме механика доезжает до сущности целиком. */
    @Test
    void storesAuthoredMechanics() {
        CreatureActionEffect effect = new CreatureActionEffect();
        effect.setAttackType(AttackType.MELEE);
        effect.setAttackBonus(5);
        effect.setReach(10);

        DamagePart part = new DamagePart();
        part.setFormula("1к8+3@dmg.piercing");
        part.setTarget("selected");
        effect.setDamageParts(List.of(part));

        ActionRequest request = new ActionRequest();
        request.setEffect(effect);

        CreatureAction stored = mapper.toEntity(request);

        assertEquals(5, stored.getEffect().getAttackBonus());
        assertEquals("1к8+3@dmg.piercing", stored.getEffect().getDamageParts().getFirst().getFormula());
        // Легаси-поля держатся в согласии с механикой: разбор описания у соседних
        // записей читает именно их.
        assertEquals(AttackType.MELEE, stored.getAttackType());
    }

    private SawingThrow savingThrow(Ability ability, int dc) {
        SawingThrow result = new SawingThrow();
        result.setAbility(ability);
        result.setDc((byte) dc);
        return result;
    }
}

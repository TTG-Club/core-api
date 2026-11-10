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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    /**
     * Урон «или» и состояния в формуле переживают круг целиком: запрос формы → JSONB
     * записи → {@code /raw}. Порядок вариантов, подписи и части не меняются.
     */
    @Test
    void keepsDamageAlternativesThroughFormRoundTrip() throws Exception {
        ObjectMapper json = new ObjectMapper();
        String alternatives = """
                [
                  {"condition": "formula",
                   "damageParts": [{"formula": "2к8@dmg.necrotic@self.status.bloodied + 2"}]},
                  {"condition": "ask", "label": "С преимуществом",
                   "damageParts": [{"formula": "4к6@dmg.piercing + 4", "target": "selected"},
                                   {"formula": "1к6@dmg.fire"}]}
                ]""";
        String requestJson = """
                {"name": {"rus": "Рой хватающих рук"},
                 "effect": {
                   "attackBonus": 4,
                   "damageParts": [{"formula": "1к8@dmg.piercing + 2к6@dmg.necrotic@target.status.prone"}],
                   "damageAlternatives": %s}}""".formatted(alternatives);

        CreatureAction stored = mapper.toEntity(json.readValue(requestJson, ActionRequest.class));
        CreatureAction reloaded = json.readValue(json.writeValueAsString(stored), CreatureAction.class);
        JsonNode raw = json.valueToTree(mapper.toRequest(reloaded)).get("effect");

        assertEquals(json.readTree(alternatives), raw.get("damageAlternatives"));
        assertEquals("1к8@dmg.piercing + 2к6@dmg.necrotic@target.status.prone",
                raw.get("damageParts").get(0).get("formula").asText());
    }

    /** «Раз в N ходов» переживает круг формы: запрос → JSONB записи → {@code /raw}. */
    @Test
    void keepsCooldownTurnsThroughFormRoundTrip() throws Exception {
        ObjectMapper json = new ObjectMapper();
        ActionRequest request = json.readValue("""
                {"name": {"rus": "Ледяной вихрь"}, "cooldownTurns": 3}""",
                ActionRequest.class);

        CreatureAction stored = mapper.toEntity(request);
        CreatureAction reloaded = json.readValue(json.writeValueAsString(stored), CreatureAction.class);
        ActionRequest raw = mapper.toRequest(reloaded);

        assertEquals(3, stored.getCooldownTurns());
        assertEquals(3, raw.getCooldownTurns());
        assertEquals(3, mapper.toEntity(raw).getCooldownTurns());
    }

    /**
     * Число ходов, которое система не примет, не сохраняется: вне 2–20 или рядом с
     * перезарядкой (её система берёт вместо него). Пустое в JSONB не пишется.
     */
    @Test
    void dropsInapplicableCooldownTurns() throws Exception {
        ActionRequest everyTurn = new ActionRequest();
        everyTurn.setCooldownTurns(1);
        ActionRequest tooRare = new ActionRequest();
        tooRare.setCooldownTurns(21);
        ActionRequest withRecharge = new ActionRequest();
        withRecharge.setRecharge(RechargeType.D5);
        withRecharge.setCooldownTurns(3);

        CreatureAction stored = mapper.toEntity(withRecharge);

        assertNull(mapper.toEntity(everyTurn).getCooldownTurns());
        assertNull(mapper.toEntity(tooRare).getCooldownTurns());
        assertNull(stored.getCooldownTurns());
        assertEquals(RechargeType.D5, stored.getRecharge());
        assertFalse(new ObjectMapper().writeValueAsString(stored).contains("cooldownTurns"));
    }

    private SawingThrow savingThrow(Ability ability, int dc) {
        SawingThrow result = new SawingThrow();
        result.setAbility(ability);
        result.setDc((byte) dc);
        return result;
    }
}

package club.ttg.dnd5.domain.common.model;

import club.ttg.dnd5.domain.spell.model.SpellEffect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Контракт JSONB с мастерской сайта: поля, которые шлёт редактор заклинания,
 * обязаны пережить сериализацию в обе стороны.
 *
 * <p>Неизвестные поля Jackson молча выбрасывает, поэтому пропущенное поле в
 * модели выглядит как «сайт ничего не прислал» — такой тест ловит это раньше
 * продакшена.</p>
 */
class VttgJsonContractTest {
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    void spellEffectKeepsParallelDamagePartArrays() throws Exception {
        String json = """
                {
                  "damageFormulas": ["3к6@dmg.necrotic@target.type.undead", "1к6@heal"],
                  "damageFormulaTargets": ["selected", "self"],
                  "damageFormulaRequiresDamage": [false, true]
                }
                """;

        SpellEffect effect = mapper.readValue(json, SpellEffect.class);

        assertEquals(List.of("selected", "self"), effect.getDamageFormulaTargets());
        assertEquals(List.of(false, true), effect.getDamageFormulaRequiresDamage());

        String serialized = mapper.writeValueAsString(effect);

        assertTrue(serialized.contains("damageFormulaRequiresDamage"));
    }

    @Test
    void spellEffectKeepsDeliveryAttackBonusAndScaling() throws Exception {
        String json = """
                {
                  "deliveryType": "sight",
                  "attackBonus": 2,
                  "scaling": {
                    "additionalDice": "1к6",
                    "additionalTargets": 1,
                    "description": "Урон увеличивается на 1к6 за круг."
                  },
                  "cantripScalingTiers": [
                    {
                      "level": 5,
                      "parts": [
                        { "formula": "2к12@dmg.necrotic", "target": "selected", "requiresDamage": true }
                      ]
                    }
                  ]
                }
                """;

        SpellEffect effect = mapper.readValue(json, SpellEffect.class);

        assertEquals("sight", effect.getDeliveryType());
        assertEquals(2, effect.getAttackBonus());
        assertEquals("1к6", effect.getScaling().getAdditionalDice());
        assertEquals(1, effect.getScaling().getAdditionalTargets());
        assertEquals(5, effect.getCantripScalingTiers().getFirst().getLevel());
        assertEquals("2к12@dmg.necrotic",
                effect.getCantripScalingTiers().getFirst().getParts().getFirst().getFormula());
        assertEquals(Boolean.TRUE,
                effect.getCantripScalingTiers().getFirst().getParts().getFirst().getRequiresDamage());

        String serialized = mapper.writeValueAsString(effect);

        assertTrue(serialized.contains("\"deliveryType\":\"sight\""));
        assertTrue(serialized.contains("\"attackBonus\":2"));
        assertTrue(serialized.contains("\"cantripScalingTiers\""));
    }

    @Test
    void activeEffectKeepsTurnDurationAndOneShotFields() throws Exception {
        String json = """
                {
                  "id": "effect-1",
                  "name": "Луч слабости",
                  "description": "",
                  "disabled": false,
                  "origin": "spell",
                  "transfer": false,
                  "duration": { "type": "turn", "turnAnchor": "source", "turnTiming": "end" },
                  "changes": [],
                  "flags": ["attack.disadvantage"],
                  "consumeOn": "carrierAttack",
                  "applyOnSuccessOnly": true,
                  "conditionImmunities": ["poisoned"],
                  "damageParts": [
                    { "formula": "2к8@dmg.poison", "target": "selected", "requiresDamage": true }
                  ]
                }
                """;

        ActiveEffect effect = mapper.readValue(json, ActiveEffect.class);

        assertEquals("turn", effect.getDuration().getType());
        assertEquals("source", effect.getDuration().getTurnAnchor());
        assertEquals("end", effect.getDuration().getTurnTiming());
        assertEquals("carrierAttack", effect.getConsumeOn());
        assertEquals(Boolean.TRUE, effect.getApplyOnSuccessOnly());
        assertEquals(List.of("poisoned"), effect.getConditionImmunities());
        assertEquals(Boolean.TRUE, effect.getDamageParts().getFirst().getRequiresDamage());
        // Тип урона живёт токеном формулы: отдельное поле мастерская больше не шлёт.
        assertNull(effect.getDamageParts().getFirst().getType());

        String serialized = mapper.writeValueAsString(effect);

        assertTrue(serialized.contains("\"turnAnchor\":\"source\""));
        assertTrue(serialized.contains("\"turnTiming\":\"end\""));
        assertTrue(serialized.contains("\"consumeOn\":\"carrierAttack\""));
        assertTrue(serialized.contains("\"applyOnSuccessOnly\":true"));
        assertTrue(serialized.contains("\"conditionImmunities\":[\"poisoned\"]"));
        assertTrue(serialized.contains("\"requiresDamage\":true"));
    }

    /**
     * Флаги — свободный список строк, и бэкенд их не знает. Тест закрепляет
     * именно это: словарь растёт на стороне системы D&D, а сюда новые ключи
     * («против магии», понавыковые) обязаны доезжать без правок модели.
     */
    @Test
    void activeEffectKeepsUnknownToBackendFlagKeys() throws Exception {
        String json = """
                {
                  "id": "effect-2",
                  "name": "Мантия сопротивления заклинаниям",
                  "origin": "item",
                  "transfer": true,
                  "duration": { "type": "permanent" },
                  "changes": [],
                  "flags": ["save.advantage.vsMagic", "skill.perception.advantage"]
                }
                """;

        ActiveEffect effect = mapper.readValue(json, ActiveEffect.class);

        assertEquals(
                List.of("save.advantage.vsMagic", "skill.perception.advantage"),
                effect.getFlags());

        String serialized = mapper.writeValueAsString(effect);

        assertTrue(serialized.contains("\"save.advantage.vsMagic\""));
        assertTrue(serialized.contains("\"skill.perception.advantage\""));
    }

    /**
     * Условный урон по виду атаки описывается изменением с ключом
     * {@code damage.ranged} и кость-формулой в значении: движок системы
     * собирает такие в момент броска. Отдельного поля условия у части урона для
     * этого не нужно, и тест держит форму, на которую опираются записи каталога.
     */
    @Test
    void activeEffectKeepsDiceValuedDamageChange() throws Exception {
        String json = """
                {
                  "id": "effect-3",
                  "name": "Дварфийский метатель",
                  "origin": "item",
                  "transfer": true,
                  "duration": { "type": "permanent" },
                  "flags": [],
                  "changes": [
                    {
                      "key": "damage.ranged",
                      "mode": "add",
                      "value": "1к8@dmg.force",
                      "priority": 20
                    }
                  ]
                }
                """;

        ActiveEffect effect = mapper.readValue(json, ActiveEffect.class);
        ActiveEffect.Change change = effect.getChanges().getFirst();

        assertEquals("damage.ranged", change.getKey());
        assertEquals("1к8@dmg.force", change.getValue());

        String serialized = mapper.writeValueAsString(effect);

        assertTrue(serialized.contains("\"key\":\"damage.ranged\""));
    }
}

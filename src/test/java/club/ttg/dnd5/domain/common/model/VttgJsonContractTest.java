package club.ttg.dnd5.domain.common.model;

import club.ttg.dnd5.domain.beastiary.model.action.AttackType;
import club.ttg.dnd5.domain.beastiary.model.action.CreatureActionEffect;
import club.ttg.dnd5.domain.beastiary.rest.dto.ActionRequest;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.RechargeType;
import club.ttg.dnd5.domain.spell.model.SpellEffect;
import club.ttg.dnd5.domain.spell.model.enums.AreaOfEffectType;
import club.ttg.dnd5.domain.spell.model.enums.SpellSaveEffect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Контракт JSONB с мастерской сайта: поля, которые шлёт редактор заклинания и
 * редактор существа, обязаны пережить сериализацию в обе стороны.
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

    /**
     * Механика действия существа: ровно то, что шлёт форма мастерской. Неизвестное поле
     * Jackson молча выбросит, и пропущенный геттер выглядел бы как «сайт ничего не прислал».
     */
    @Test
    void creatureActionKeepsAuthoredMechanics() throws Exception {
        String json = """
                {
                  "name": { "rus": "Укус", "eng": "Bite" },
                  "description": "Кусает",
                  "recharge": "D5",
                  "effect": {
                    "attackType": "MELEE",
                    "attackBonus": 9,
                    "reach": 10,
                    "rangeNormal": 30,
                    "rangeLong": 120,
                    "damageParts": [
                      { "formula": "2к10 + 4@dmg.piercing", "target": "selected", "requiresDamage": false }
                    ],
                    "savingThrows": [ { "ability": "DEXTERITY", "dc": 17 } ],
                    "saveEffect": "HALF",
                    "areaOfEffect": { "type": "CONE", "value1": 30 },
                    "damageTypes": ["PIERCING"],
                    "activeEffects": [
                      { "id": "e1", "name": "Отравление", "origin": "feature", "effectTarget": "target" }
                    ]
                  }
                }
                """;

        ActionRequest request = mapper.readValue(json, ActionRequest.class);
        CreatureActionEffect effect = request.getEffect();

        assertEquals(RechargeType.D5, request.getRecharge());
        assertEquals(AttackType.MELEE, effect.getAttackType());
        assertEquals(9, effect.getAttackBonus());
        assertEquals(10, effect.getReach());
        assertEquals(30, effect.getRangeNormal());
        assertEquals(120, effect.getRangeLong());
        assertEquals("2к10 + 4@dmg.piercing", effect.getDamageParts().getFirst().getFormula());
        assertEquals("selected", effect.getDamageParts().getFirst().getTarget());
        assertEquals(Ability.DEXTERITY, effect.getSavingThrows().iterator().next().getAbility());
        assertEquals(17, effect.getSavingThrows().iterator().next().getDc());
        assertEquals(SpellSaveEffect.HALF, effect.getSaveEffect());
        assertEquals(AreaOfEffectType.CONE, effect.getAreaOfEffect().getType());
        assertEquals(30, effect.getAreaOfEffect().getValue1());
        assertEquals(List.of(DamageType.PIERCING), List.copyOf(effect.getDamageTypes()));
        assertEquals("target", effect.getActiveEffects().getFirst().getEffectTarget());
    }

    /**
     * Незаполненная механика: форма шлёт её у каждой записи, и она НЕ должна выглядеть
     * заведённой — иначе выгрузка бросила бы разбор описания у записи, где ничего не завели.
     */
    @Test
    void creatureActionWithoutMechanicsKeepsAreaShapeless() throws Exception {
        String json = """
                {
                  "name": { "rus": "Умение", "eng": "" },
                  "description": "Просто текст",
                  "effect": {
                    "damageParts": [],
                    "savingThrows": [],
                    "areaOfEffect": {},
                    "damageTypes": [],
                    "activeEffects": []
                  }
                }
                """;

        ActionRequest request = mapper.readValue(json, ActionRequest.class);
        CreatureActionEffect effect = request.getEffect();

        assertNull(effect.getAttackBonus());
        assertTrue(effect.getDamageParts().isEmpty());
        assertNull(effect.getAreaOfEffect().getType());
    }
}

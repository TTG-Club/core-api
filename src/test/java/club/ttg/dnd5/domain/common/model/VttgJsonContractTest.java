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
import com.fasterxml.jackson.databind.JsonNode;
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
     * Спасбросок урона каждый ход: {@code dc = 0} — это «Сл наложившего», а не
     * «не задано», поэтому ноль обязан пережить круг, а не выпасть как пустое.
     */
    @Test
    void activeEffectKeepsRecurringDamageSaveWithSourceDc() throws Exception {
        String json = """
                {
                  "id": "effect-4",
                  "name": "Кислота",
                  "effectTarget": "target",
                  "recurringDamage": {
                    "damageParts": [{ "formula": "2к4@dmg.acid" }],
                    "timing": "endOfTurn",
                    "save": { "ability": "constitution", "dc": 0, "onSuccess": "half" }
                  }
                }
                """;

        ActiveEffect effect = mapper.readValue(json, ActiveEffect.class);
        ActiveEffect.Save save = effect.getRecurringDamage().getSave();

        assertEquals("constitution", save.getAbility());
        assertEquals(0, save.getDc());
        assertEquals("half", save.getOnSuccess());

        String serialized = mapper.writeValueAsString(effect);

        assertTrue(serialized.contains(
                "\"save\":{\"ability\":\"constitution\",\"dc\":0,\"onSuccess\":\"half\"}"));
    }

    @Test
    void activeEffectKeepsExhaustionLevel() throws Exception {
        String json = """
                {
                  "id": "effect-5",
                  "name": "Истощение",
                  "conditionKey": "exhaustion",
                  "exhaustionLevel": 2
                }
                """;

        ActiveEffect effect = mapper.readValue(json, ActiveEffect.class);

        assertEquals(2, effect.getExhaustionLevel());

        String serialized = mapper.writeValueAsString(effect);

        assertTrue(serialized.contains("\"exhaustionLevel\":2"));
    }

    /**
     * Срабатывания бэкенд не разбирает: элемент обязан вернуться ровно таким, каким
     * его прислала мастерская, — с нулевой Сл, строками условий в кавычках и
     * событиями, которых текущий движок ещё не знает ({@code rest}).
     */
    @Test
    void activeEffectKeepsTriggersAsAuthored() throws Exception {
        String json = """
                {
                  "id": "effect-6",
                  "name": "Срабатывания",
                  "triggers": [
                    {
                      "id": "trigger_moonbeam_enter",
                      "event": "enter",
                      "save": { "ability": "constitution", "dc": 0 },
                      "actions": [
                        { "type": "damage", "parts": [{ "formula": "2d10@dmg.radiant" }], "on": "always", "halfOnSave": true }
                      ],
                      "limit": { "max": 1, "per": "turn", "key": "moonbeam" }
                    },
                    {
                      "id": "trigger_stench",
                      "event": "turnStart",
                      "save": { "ability": "constitution", "dc": 12 },
                      "actions": [
                        { "type": "applyCondition", "conditionKey": "poisoned", "duration": { "type": "rounds", "value": 1 } }
                      ]
                    },
                    {
                      "id": "trigger_fire",
                      "event": "damageTaken",
                      "condition": "damage.type === \\"fire\\"",
                      "actions": [{ "type": "applyTag", "tag": "noRegen", "label": "Без регенерации" }]
                    },
                    {
                      "id": "trigger_fortitude",
                      "event": "hpZero",
                      "condition": "damage.type !== \\"radiant\\" && damage.isCritical === false",
                      "save": { "ability": "constitution", "dc": 5, "dcFormula": "5 + @damage" },
                      "actions": [{ "type": "setHp", "value": 1, "on": "saved" }]
                    },
                    {
                      "id": "trigger_fire_aura",
                      "event": "turnEnd",
                      "turnOf": "source",
                      "actions": [{ "type": "damage", "parts": [{ "formula": "1d10@dmg.fire" }] }]
                    },
                    {
                      "id": "trigger_rest",
                      "event": "rest",
                      "actions": [{ "type": "removeSelf" }]
                    }
                  ]
                }
                """;

        ActiveEffect effect = mapper.readValue(json, ActiveEffect.class);

        assertEquals(6, effect.getTriggers().size());
        assertEquals("damage.type === \"fire\"", effect.getTriggers().get(2).get("condition").asText());
        assertEquals("rest", effect.getTriggers().getLast().get("event").asText());

        String serialized = mapper.writeValueAsString(effect);
        String authoredTriggers = mapper.writeValueAsString(mapper.readTree(json).get("triggers"));

        // Байт-в-байт с присланным: порядок ключей, типы значений, экранирование.
        assertTrue(serialized.contains("\"triggers\":" + authoredTriggers));
        assertTrue(serialized.contains("\"save\":{\"ability\":\"constitution\",\"dc\":0}"));
        assertTrue(serialized.contains("\"condition\":\"damage.type === \\\"fire\\\"\""));
        assertTrue(serialized.contains("\"event\":\"rest\""));
    }

    /**
     * Поля системы 0.8.62: применение и включение, вариант, условие наложения,
     * условие броска и аура с радиусом формулой. Без них редактор сохранял
     * настроенный эффект, а компендиум получал предмет, который просто надет.
     */
    @Test
    void activeEffectKeepsActivationVariantAndConditions() throws Exception {
        String json = """
                {
                  "id": "effect-activation",
                  "name": "Ярость",
                  "activation": { "mode": "toggle", "counter": "rages", "amount": 2 },
                  "variant": { "group": "вариант", "label": "Оглушение", "pick": "random" },
                  "landingCondition": "source.weaponMastery === true",
                  "rollCondition": "target.allyAdjacent",
                  "aura": {
                    "radius": 10,
                    "target": "allies",
                    "applyToSelf": true,
                    "radiusFormula": "10 + 20 * floor(@classLevel / 18)",
                    "whileCapable": true
                  }
                }
                """;

        ActiveEffect effect = mapper.readValue(json, ActiveEffect.class);

        assertEquals("toggle", effect.getActivation().getMode());
        assertEquals("rages", effect.getActivation().getCounter());
        assertEquals(2, effect.getActivation().getAmount());
        assertEquals("вариант", effect.getVariant().getGroup());
        assertEquals("random", effect.getVariant().getPick());
        assertEquals("source.weaponMastery === true", effect.getLandingCondition());
        assertEquals("target.allyAdjacent", effect.getRollCondition());
        assertEquals("10 + 20 * floor(@classLevel / 18)", effect.getAura().getRadiusFormula());
        assertEquals(Boolean.TRUE, effect.getAura().getWhileCapable());

        String serialized = mapper.writeValueAsString(effect);

        assertTrue(serialized.contains("\"activation\":{\"mode\":\"toggle\",\"counter\":\"rages\",\"amount\":2}"));
        assertTrue(serialized.contains("\"landingCondition\":\"source.weaponMastery === true\""));
        assertTrue(serialized.contains("\"rollCondition\":\"target.allyAdjacent\""));
        assertTrue(serialized.contains("\"radiusFormula\":\"10 + 20 * floor(@classLevel / 18)\""));
        assertTrue(serialized.contains("\"whileCapable\":true"));
    }

    /**
     * Срабатывания 0.8.62 идут как есть: получатель «всем в радиусе» с радиусом,
     * отдых, режим спасброска, уменьшение максимума хитов, повторный спасбросок
     * наложенного состояния и счётчик отметки.
     */
    @Test
    void activeEffectKeepsAreaRecipientRestAndMaxHpTriggers() throws Exception {
        String json = """
                {
                  "id": "effect-triggers-v3",
                  "name": "Споры и отдых",
                  "triggers": [
                    {
                      "id": "trigger_spores",
                      "event": "hpZero",
                      "recipient": "area",
                      "area": { "radius": 10, "target": "enemies" },
                      "save": { "ability": "constitution", "dc": 12, "mode": "advantage" },
                      "actions": [
                        {
                          "type": "applyCondition",
                          "conditionKey": "poisoned",
                          "recurringSave": { "ability": "constitution", "dc": 12, "timing": "endOfTurn" }
                        },
                        { "type": "applyTag", "tag": "spores", "stack": true }
                      ]
                    },
                    {
                      "id": "trigger_drain",
                      "event": "applied",
                      "actions": [{ "type": "reduceMaxHp", "amount": "@damage", "endsOnRest": "long" }]
                    },
                    {
                      "id": "trigger_rest",
                      "event": "rest",
                      "restType": "short",
                      "actions": [{ "type": "removeSelf" }]
                    }
                  ]
                }
                """;

        ActiveEffect effect = mapper.readValue(json, ActiveEffect.class);
        String serialized = mapper.writeValueAsString(effect);
        String authoredTriggers = mapper.writeValueAsString(mapper.readTree(json).get("triggers"));

        // Байт-в-байт: срабатывания лежат JsonNode и не разбираются по полям
        assertTrue(serialized.contains("\"triggers\":" + authoredTriggers));
        assertEquals("area", effect.getTriggers().getFirst().get("recipient").asText());
        assertEquals(10, effect.getTriggers().getFirst().get("area").get("radius").asInt());
        assertEquals("short", effect.getTriggers().getLast().get("restType").asText());
    }

    /**
     * Срабатывания 0.9: получатель «по выбору» со своим блоком и собственные
     * срабатывания наложенного состояния. Оба поля лежат ВНУТРИ
     * {@code triggers} — модель их не знает и знать не должна, но круг обязан
     * быть байт-в-байт: иначе автор заполнит их на сайте, а в компендиум
     * доедет пустое наложение.
     */
    @Test
    void activeEffectKeepsChoiceRecipientAndNestedTriggers() throws Exception {
        String json = """
                {
                  "id": "effect-triggers-choice",
                  "name": "Аура живучести и Сон",
                  "triggers": [
                    {
                      "id": "trigger_heal",
                      "event": "turnStart",
                      "recipient": "choice",
                      "choice": {
                        "radius": 30,
                        "target": "allies",
                        "count": 2,
                        "condition": "self.creatureType === \\"undead\\"",
                        "optional": true,
                        "chooser": "source"
                      },
                      "actions": [
                        { "type": "damage", "parts": [{ "formula": "5@heal" }] }
                      ]
                    },
                    {
                      "id": "trigger_sleep",
                      "event": "applied",
                      "actions": [
                        {
                          "type": "applyCondition",
                          "conditionKey": "unconscious",
                          "triggers": [
                            {
                              "id": "trigger_wake",
                              "event": "damageTaken",
                              "actions": [{ "type": "removeSelf" }]
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
                """;

        ActiveEffect effect = mapper.readValue(json, ActiveEffect.class);
        String serialized = mapper.writeValueAsString(effect);
        String authoredTriggers = mapper.writeValueAsString(mapper.readTree(json).get("triggers"));

        assertTrue(serialized.contains("\"triggers\":" + authoredTriggers));

        JsonNode choice = effect.getTriggers().getFirst().get("choice");

        assertEquals("choice", effect.getTriggers().getFirst().get("recipient").asText());
        assertEquals(30, choice.get("radius").asInt());
        assertEquals(2, choice.get("count").asInt());
        assertEquals("source", choice.get("chooser").asText());
        assertTrue(choice.get("optional").asBoolean());
        assertEquals("self.creatureType === \"undead\"", choice.get("condition").asText());

        JsonNode nested = effect
                .getTriggers()
                .getLast()
                .get("actions")
                .get(0)
                .get("triggers")
                .get(0);

        assertEquals("damageTaken", nested.get("event").asText());
        assertEquals("removeSelf", nested.get("actions").get(0).get("type").asText());
    }

    /**
     * Зона заклинания — ещё одно значение строки {@code effectTarget}, а лечение
     * каждый ход — токен {@code @heal} в формуле части: модели правки не нужны,
     * но оба обязаны доходить как есть.
     */
    @Test
    void activeEffectKeepsSpellZoneDeliveryAndRecurringHealing() throws Exception {
        String json = """
                {
                  "id": "effect-7",
                  "name": "Зона лечения",
                  "effectTarget": "zone",
                  "areaTrigger": "stay",
                  "recurringDamage": {
                    "damageParts": [{ "formula": "10@heal" }],
                    "timing": "startOfTurn"
                  }
                }
                """;

        ActiveEffect effect = mapper.readValue(json, ActiveEffect.class);

        assertEquals("zone", effect.getEffectTarget());
        assertEquals("10@heal", effect.getRecurringDamage().getDamageParts().getFirst().getFormula());
        assertNull(effect.getRecurringDamage().getSave());

        String serialized = mapper.writeValueAsString(effect);

        assertTrue(serialized.contains("\"effectTarget\":\"zone\""));
        assertTrue(serialized.contains("\"formula\":\"10@heal\""));
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

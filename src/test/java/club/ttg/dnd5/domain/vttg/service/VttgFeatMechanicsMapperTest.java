package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.SenseType;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.model.AbilityBonus;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceGrant;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceOption;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceType;
import club.ttg.dnd5.domain.common.model.mechanics.DamageAffinity;
import club.ttg.dnd5.domain.common.model.mechanics.MechanicChoice;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.common.model.mechanics.SheetModifiers;
import club.ttg.dnd5.domain.common.model.mechanics.HitPointsModifier;
import club.ttg.dnd5.domain.common.model.mechanics.ProficiencyGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SenseGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SpeedModifier;
import club.ttg.dnd5.domain.feat.model.prerequisite.AbilityRequirement;
import club.ttg.dnd5.domain.feat.model.prerequisite.ClassFeatureRequirement;
import club.ttg.dnd5.domain.feat.model.prerequisite.FeatPrerequisite;
import club.ttg.dnd5.domain.source.model.Source;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Выгрузка требований и механики черты. Проверяется прежде всего перевод словарей:
 * лист персонажа ждёт слаги, а модель хранит enum'ы, и молчаливое расхождение здесь
 * не ломает выгрузку — оно просто делает механику неработающей.
 */
class VttgFeatMechanicsMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final VttgFeatMapper mapper = new VttgFeatMapper(
            new VttgMarkupConverter(objectMapper), new VttgFeatMechanicsMapper());

    /** Черта без механики полей не несёт — вместо пустых блоков их нет вовсе. */
    @Test
    void omitsEmptyBlocks() {
        JsonNode json = json(baseFeat());

        assertFalse(json.has("featData"));
        assertFalse(json.has("mechanics"));
    }

    /** Требование «Сила ИЛИ Ловкость 13+» с уровнем и классовым умением. */
    @Test
    void mapsPrerequisite() {
        Feat feat = baseFeat();
        FeatPrerequisite prerequisite = new FeatPrerequisite();
        prerequisite.setMinCharacterLevel(4);
        prerequisite.setAbilities(List.of(
                new AbilityRequirement(Set.of(Ability.STRENGTH, Ability.DEXTERITY), 13)));
        prerequisite.setClassFeatures(Set.of(ClassFeatureRequirement.SPELLCASTING));
        prerequisite.setArmorProficiency(Set.of(ArmorCategory.MEDIUM));
        prerequisite.setClasses(List.of(new EntityRef("wizard-phb", "Волшебник")));
        prerequisite.setCampaign("Эберрон");
        feat.setPrerequisiteDetails(prerequisite);

        JsonNode json = json(feat).get("featData").get("prerequisite");
        assertEquals(4, json.get("minLevel").asInt());

        JsonNode requirement = json.get("abilityRequirements").get(0);
        assertEquals(13, requirement.get("minValue").asInt());
        assertTrue(names(requirement.get("anyOf")).containsAll(List.of("strength", "dexterity")));

        assertEquals("spellcasting", json.get("classFeatures").get(0).asText());
        assertEquals("medium", json.get("armorProficiency").get(0).asText());
        assertEquals("wizard-phb", json.get("classes").get(0).get("url").asText());
        assertEquals("Волшебник", json.get("classes").get(0).get("name").asText());
        assertEquals("Эберрон", json.get("campaign").asText());
    }

    /**
     * Требование, из которого ничего не разобралось, поля не создаёт: человекочитаемая
     * строка всё равно остаётся в описании черты.
     */
    @Test
    void omitsPrerequisiteWithoutParsedFields() {
        Feat feat = baseFeat();
        feat.setPrerequisiteDetails(new FeatPrerequisite());

        assertFalse(json(feat).has("featData"));
    }

    /** «Улучшение характеристик»: два взаимоисключающих варианта повышения. */
    @Test
    void mapsAbilityBonusVariants() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setAbilityBonuses(List.of(
                abilityBonus(List.of(Ability.STRENGTH, Ability.DEXTERITY), 2, 20, 1),
                abilityBonus(List.of(Ability.STRENGTH, Ability.DEXTERITY), 1, 20, 2)));
        feat.setMechanics(mechanics);

        JsonNode bonuses = json(feat).get("mechanics").get("abilityBonuses");
        assertEquals(2, bonuses.size());
        assertEquals(2, bonuses.get(0).get("bonus").asInt());
        assertEquals(1, bonuses.get(0).get("count").asInt());
        assertEquals(1, bonuses.get(1).get("bonus").asInt());
        assertEquals(2, bonuses.get(1).get("count").asInt());
        assertEquals(20, bonuses.get(0).get("upto").asInt());
    }

    /** Владения без выбора: категории оружия сворачиваются в simple/martial, навык — в camelCase. */
    @Test
    void mapsProficiencyGrant() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        ProficiencyGrant grant = new ProficiencyGrant();
        grant.setWeaponCategories(Set.of(WeaponCategory.MATERIAL_MELEE, WeaponCategory.MATERIAL_RANGED));
        grant.setArmorCategories(Set.of(ArmorCategory.SHIELD));
        grant.setSkills(Set.of(Skill.SLEIGHT_OF_HAND));
        grant.setTools(List.of(new EntityRef("thieves-tools-phb", "Воровские инструменты")));
        mechanics.setProficiencies(grant);
        feat.setMechanics(mechanics);

        JsonNode json = json(feat);
        JsonNode featData = json.get("featData");

        // Воинское рукопашное и дальнобойное — одна категория правил
        assertEquals(1, featData.get("weaponProficiencies").size());
        assertEquals("martial", featData.get("weaponProficiencies").get(0).asText());
        assertEquals("shield", featData.get("armorProficiencies").get(0).asText());
        assertEquals("sleightOfHand", featData.get("skillProficiencies").get(0).asText());

        // Инструменты лист применить не может — они остаются в mechanics и в
        // featData не дублируются
        assertEquals("thieves-tools-phb",
                json.get("mechanics").get("proficiencies").get("tools").get(0).get("url").asText());
        assertFalse(featData.has("toolProficiencies"));
    }

    /** Постоянные модификаторы: хиты, скорости, КД, чувства, защиты, тип существа. */
    @Test
    void mapsModifiers() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        SheetModifiers modifiers = new SheetModifiers();

        HitPointsModifier hitPoints = new HitPointsModifier();
        hitPoints.setPerAcquisitionLevel(2);
        hitPoints.setPerLevelAfterAcquisition(2);
        modifiers.setHitPoints(hitPoints);

        SpeedModifier speed = new SpeedModifier();
        speed.setWalkBonus(10);
        speed.setClimbEqualsWalk(Boolean.TRUE);
        modifiers.setSpeed(speed);

        modifiers.setArmorClassBonus(1);
        modifiers.setSenses(List.of(new SenseGrant(SenseType.BLINDSIGHT, 10)));
        modifiers.setTelepathyRange(120);

        DamageAffinity damage = new DamageAffinity();
        damage.setResistances(Set.of(DamageType.FIRE));
        modifiers.setDamage(damage);

        modifiers.setConditionImmunities(Set.of(Condition.POISONED));
        modifiers.setCreatureType(CreatureType.UNDEAD);
        modifiers.setInitiativeProficiencyBonus(Boolean.TRUE);

        mechanics.setModifiers(modifiers);
        feat.setMechanics(mechanics);

        JsonNode featData = json(feat).get("featData");
        JsonNode json = featData.get("modifiers");

        assertEquals(2, json.get("hitPoints").get("perAcquisitionLevel").asInt());
        assertEquals(2, json.get("hitPoints").get("perLevelAfterAcquisition").asInt());
        assertEquals(10, json.get("speed").get("walkBonus").asInt());
        assertTrue(json.get("speed").get("climbEqualsWalk").asBoolean());
        assertEquals(1, json.get("armorClassBonus").asInt());
        assertEquals("blindsight", json.get("senses").get(0).get("type").asText());
        assertEquals(10, json.get("senses").get(0).get("range").asInt());
        assertEquals(120, json.get("telepathyRange").asInt());
        assertTrue(json.get("initiativeProficiencyBonus").asBoolean());

        // Защиты и иммунитеты — своими полями блока даров, а не внутри модификаторов
        assertEquals("fire", featData.get("damageDefenses").get(0).get("damageType").asText());
        assertEquals("resistance", featData.get("damageDefenses").get(0).get("kind").asText());
        assertEquals("poisoned", featData.get("conditionImmunities").get(0).asText());
        assertFalse(json.has("damage"));

        // Смену типа существа лист не применяет — она остаётся в mechanics
        assertEquals("undead", json(feat).get("mechanics").get("creatureType").asText());
    }

    /**
     * Тёмное зрение в общий список чувств не идёт: у потребителя оно живёт зрением
     * токена и приезжает своим полем.
     */
    @Test
    void skipsDarkvisionAmongSenses() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        SheetModifiers modifiers = new SheetModifiers();
        modifiers.setSenses(List.of(new SenseGrant(SenseType.DARKVISION, 60)));
        mechanics.setModifiers(modifiers);
        feat.setMechanics(mechanics);

        // Механики нет: единственное чувство ушло своим полем блока даров
        assertFalse(json(feat).has("mechanics"));
        assertEquals(60, json(feat).get("featData").get("darkvision").asInt());
        assertFalse(json(feat).get("featData").has("modifiers"));
    }

    /** Выбор навыка: тип в camelCase, флаги — только взведённые, компетентность явна. */
    @Test
    void mapsChoice() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();

        MechanicChoice choice = new MechanicChoice();
        choice.setKey("expertise-skill");
        choice.setType(ChoiceType.SKILL);
        choice.setLabel("Выберите навык");
        choice.setCount(2);
        choice.setOnlyIfProficient(Boolean.TRUE);
        choice.setGrants(ChoiceGrant.EXPERTISE);
        choice.setRechooseOnLongRest(Boolean.FALSE);
        choice.setOptions(List.of(new ChoiceOption("STEALTH", "Скрытность")));

        mechanics.setChoices(List.of(choice));
        feat.setMechanics(mechanics);

        JsonNode json = json(feat).get("featData").get("choices").get(0);
        assertEquals("expertise-skill", json.get("key").asText());
        assertEquals("skill", json.get("type").asText());
        assertEquals(2, json.get("count").asInt());
        assertTrue(json.get("onlyIfProficient").asBoolean());
        assertEquals("expertise", json.get("grants").asText());
        assertEquals("STEALTH", json.get("options").get(0).get("value").asText());
        // Снятый флаг не выводится: ложь ничего не сообщает и висела бы у каждого выбора
        assertFalse(json.has("rechooseOnLongRest"));
        assertFalse(json.has("onlyIfNotProficient"));
    }

    /** Обычный выбор даёт владение — исход по умолчанию у потребителя, поле опускается. */
    @Test
    void omitsDefaultChoiceGrant() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();

        MechanicChoice choice = new MechanicChoice();
        choice.setKey("skill");
        choice.setType(ChoiceType.SKILL);
        mechanics.setChoices(List.of(choice));
        feat.setMechanics(mechanics);

        JsonNode json = json(feat).get("featData").get("choices").get(0);
        assertNull(json.get("grants"));
        // Незаполненное количество — это один выбор
        assertEquals(1, json.get("count").asInt());
    }

    private List<String> names(JsonNode array) {
        return objectMapper.convertValue(array, objectMapper.getTypeFactory()
                .constructCollectionType(List.class, String.class));
    }

    private AbilityBonus abilityBonus(List<Ability> abilities, int bonus, int upto, int count) {
        AbilityBonus result = new AbilityBonus();
        result.setAbilities(abilities);
        result.setBonus(bonus);
        result.setUpto(upto);
        result.setCount(count);
        return result;
    }

    private JsonNode json(Feat feat) {
        return objectMapper.valueToTree(mapper.toVttg(feat));
    }

    private Feat baseFeat() {
        Feat feat = new Feat();
        feat.setUrl("alert-phb");
        feat.setName("Бдительный");
        feat.setEnglish("Alert");
        feat.setDescription("");
        Source source = new Source();
        source.setAcronym("PHB24");
        source.setName("PHB 2024");
        feat.setSource(source);
        feat.setSrdVersion("5.1");
        return feat;
    }
}

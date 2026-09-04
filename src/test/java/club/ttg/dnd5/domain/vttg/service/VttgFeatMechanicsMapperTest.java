package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Language;
import club.ttg.dnd5.domain.common.dictionary.SenseType;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.item.model.weapon.Mastery;
import club.ttg.dnd5.domain.common.model.AbilityBonus;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceGrant;
import club.ttg.dnd5.domain.common.model.mechanics.ClassSpellListGrant;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceOption;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceType;
import club.ttg.dnd5.domain.common.model.mechanics.DamageAffinity;
import club.ttg.dnd5.domain.common.model.mechanics.DamageDefenseFromChoice;
import club.ttg.dnd5.domain.common.model.mechanics.DamageDefenseKind;
import club.ttg.dnd5.domain.common.model.mechanics.MechanicChoice;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.common.model.mechanics.SheetModifiers;
import club.ttg.dnd5.domain.common.model.mechanics.HitPointsModifier;
import club.ttg.dnd5.domain.common.model.mechanics.ProficiencyGrant;
import club.ttg.dnd5.domain.common.model.mechanics.ResourceCounter;
import club.ttg.dnd5.domain.common.model.mechanics.ResourceRecovery;
import club.ttg.dnd5.domain.common.model.mechanics.SenseGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SpeedModifier;
import club.ttg.dnd5.domain.common.model.mechanics.SpellFilter;
import club.ttg.dnd5.domain.common.model.mechanics.GrantedSpellRef;
import club.ttg.dnd5.domain.common.model.mechanics.SpellGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SpellListExpansion;
import club.ttg.dnd5.domain.common.model.mechanics.SpellListGroup;
import club.ttg.dnd5.domain.feat.model.prerequisite.AbilityRequirement;
import club.ttg.dnd5.domain.feat.model.prerequisite.ClassFeatureRequirement;
import club.ttg.dnd5.domain.feat.model.prerequisite.FeatPrerequisite;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Выгрузка требований и механики черты. Проверяется прежде всего перевод словарей:
 * лист персонажа ждёт слаги, а модель хранит enum'ы, и молчаливое расхождение здесь
 * не ломает выгрузку — оно просто делает механику неработающей.
 */
class VttgFeatMechanicsMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final VttgMarkupConverter markupConverter = new VttgMarkupConverter(objectMapper);

    private final SpellRepository spellRepository = mock(SpellRepository.class);

    private final VttgFeatMapper mapper = new VttgFeatMapper(
            markupConverter, new VttgFeatMechanicsMapper(markupConverter, spellRepository,
                    mock(FeatRepository.class)));

    {
        // По умолчанию справочник пуст: заклинания подставляют только те тесты, которым
        // они нужны, — остальным маппер не должен ходить в него вовсе
        when(spellRepository.findAllShortByUrlIn(any())).thenReturn(List.of());
    }

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
        grant.setLanguages(Set.of(Language.DWARVISH));
        mechanics.setProficiencies(grant);
        feat.setMechanics(mechanics);

        JsonNode json = json(feat);
        JsonNode featData = json.get("featData");

        // Воинское рукопашное и дальнобойное — одна категория правил
        assertEquals(1, featData.get("weaponProficiencies").size());
        assertEquals("martial", featData.get("weaponProficiencies").get(0).asText());
        assertEquals("shield", featData.get("armorProficiencies").get(0).asText());
        assertEquals("sleightOfHand", featData.get("skillProficiencies").get(0).asText());

        // Инструмент едет ключом справочника листа: слаг страницы лист молча выбросит
        assertEquals("thieves-tools", featData.get("toolProficiencies").get(0).asText());
        // Язык — русским названием: ключей у языков на листе нет
        assertEquals("Дварфийский", featData.get("languages").get(0).asText());
        // Владение применяется целиком — ссылке в mechanics делать нечего
        assertFalse(json.has("mechanics"));
    }

    /** Притяжательный апостроф: на сайте отдельный сегмент, у листа его нет вовсе. */
    @Test
    void mapsPossessiveToolUrlToSheetKey() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        ProficiencyGrant grant = new ProficiencyGrant();
        grant.setTools(List.of(
                new EntityRef("calligrapher-s-supplies-phb", "Инструменты каллиграфа"),
                new EntityRef("cook-s-utensils-phb", "Инструменты повара")));
        mechanics.setProficiencies(grant);
        feat.setMechanics(mechanics);

        JsonNode tools = json(feat).get("featData").get("toolProficiencies");
        assertEquals("calligraphers-supplies", tools.get(0).asText());
        assertEquals("cooks-utensils", tools.get(1).asText());
    }

    /**
     * Инструмент, которого в справочнике листа нет, остаётся ссылкой в механике: применить
     * его нечем, но показать нужно.
     */
    @Test
    void keepsUnknownToolAsReference() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        ProficiencyGrant grant = new ProficiencyGrant();
        grant.setTools(List.of(new EntityRef("astral-loom-hb", "Астральный ткацкий станок")));
        mechanics.setProficiencies(grant);
        feat.setMechanics(mechanics);

        JsonNode json = json(feat);
        assertFalse(json.has("featData"));
        assertEquals("astral-loom-hb",
                json.get("mechanics").get("proficiencies").get("tools").get(0).get("url").asText());
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
     * Защита по выбору игрока едет своим полем: тип урона ещё не назван, и в плоский
     * список пар «тип + вид» такая защита не укладывается.
     */
    @Test
    void exportsDamageDefensesByChoice() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        SheetModifiers modifiers = new SheetModifiers();

        DamageAffinity damage = new DamageAffinity();
        damage.setDefenseChoices(List.of(
                new DamageDefenseFromChoice("damage-type", DamageDefenseKind.RESISTANCE),
                new DamageDefenseFromChoice("second-type", DamageDefenseKind.IMMUNITY)));
        modifiers.setDamage(damage);
        mechanics.setModifiers(modifiers);
        feat.setMechanics(mechanics);

        JsonNode featData = json(feat).get("featData");
        JsonNode choices = featData.get("damageDefenseChoices");

        assertEquals("damage-type", choices.get(0).get("choiceKey").asText());
        assertEquals("resistance", choices.get(0).get("kind").asText());
        assertEquals("second-type", choices.get(1).get("choiceKey").asText());
        assertEquals("immunity", choices.get(1).get("kind").asText());

        // Легаси-поле держит первое сопротивление: сборки, не знающие о новом списке,
        // читают его и получают тот же случай, что и раньше
        assertEquals("damage-type",
                featData.get("modifiers").get("resistanceFromChoiceKey").asText());
        assertFalse(featData.has("damageDefenses"));
    }

    /** Запись, сделанная до появления списка, приходит одним легаси-полем. */
    @Test
    void unfoldsLegacyResistanceChoiceKey() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        SheetModifiers modifiers = new SheetModifiers();

        DamageAffinity damage = new DamageAffinity();
        damage.setResistanceFromChoiceKey("damage-type");
        modifiers.setDamage(damage);
        mechanics.setModifiers(modifiers);
        feat.setMechanics(mechanics);

        JsonNode featData = json(feat).get("featData");

        assertEquals("damage-type",
                featData.get("damageDefenseChoices").get(0).get("choiceKey").asText());
        assertEquals("resistance",
                featData.get("damageDefenseChoices").get(0).get("kind").asText());
        assertEquals("damage-type",
                featData.get("modifiers").get("resistanceFromChoiceKey").asText());
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
        // Значение — в словаре листа: с именем enum'а владение молча не проставится
        assertEquals("stealth", json.get("options").get(0).get("value").asText());
        assertEquals("Скрытность", json.get("options").get(0).get("name").asText());
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

    /**
     * Значения вариантов переводятся по типу выбора: каждый словарь свой, и лист кладёт
     * выбранное значение прямо во владения актора.
     */
    @Test
    void mapsChoiceOptionValuesByType() {
        assertEquals("sleightOfHand", optionValue(ChoiceType.SKILL, "SLEIGHT_OF_HAND"));
        assertEquals("constitution", optionValue(ChoiceType.SAVING_THROW, "CONSTITUTION"));
        assertEquals("charisma", optionValue(ChoiceType.SPELLCASTING_ABILITY, "CHARISMA"));
        assertEquals("fire", optionValue(ChoiceType.DAMAGE_TYPE, "FIRE"));
        // Историческая опечатка имени константы: в сохранённых данных ещё встречается
        assertEquals("fire", optionValue(ChoiceType.DAMAGE_TYPE, "FAIR"));
        assertEquals("Дварфийский", optionValue(ChoiceType.LANGUAGE, "DWARVISH"));
        assertEquals("thieves-tools", optionValue(ChoiceType.TOOL, "thieves-tools-phb"));
        assertEquals("wizard", optionValue(ChoiceType.SPELL_LIST, "wizard-phb"));
        assertEquals("longsword", optionValue(ChoiceType.WEAPON, "longsword-phb"));
        assertEquals("martial", optionValue(ChoiceType.WEAPON, "MATERIAL_MELEE"));
        // Заклинание адресуется url записи — переводить нечего
        assertEquals("fireball-phb", optionValue(ChoiceType.SPELL, "fireball-phb"));
        // «Вариант» у каждой черты свой, общего словаря нет
        assertEquals("mark-of-healing", optionValue(ChoiceType.OPTION, "mark-of-healing"));
    }

    /**
     * Значение не из словаря выбрасывается там, где у листа есть свой справочник: пустой
     * список вариантов он раскроет целиком, а чужая строка осела бы во владениях актора.
     */
    @Test
    void dropsUnknownOptionValueWhenSheetHasVocabulary() {
        assertNull(optionValue(ChoiceType.SKILL, "КРИВОЕ_ЗНАЧЕНИЕ"));
        assertNull(optionValue(ChoiceType.TOOL, "astral-loom-hb"));
        assertNull(optionValue(ChoiceType.LANGUAGE, "ЭЛЬФИЙСКИЙ"));
    }

    /**
     * У оружия и «вариантов» справочника нет — пул берётся только из этого списка, и
     * выброшенное значение оставило бы игрока с пустым выбором.
     */
    @Test
    void keepsUnknownOptionValueWithoutVocabulary() {
        assertEquals("mark-of-healing", optionValue(ChoiceType.OPTION, "mark-of-healing"));
        assertEquals("astral-blade-hb", optionValue(ChoiceType.WEAPON, "astral-blade-hb"));
    }

    /** Общий язык жестов справочнику листа незнаком, но лист хранит его как «свой язык». */
    @Test
    void mapsSignLanguageByName() {
        assertEquals("Общий язык жестов", optionValue(ChoiceType.LANGUAGE, "COMMON_SIGN_LANGUAGE"));
    }

    /**
     * Разные значения источника сходятся у потребителя в одно: рукопашное и дальнобойное
     * воинское оружие — одна категория правил, и кнопка в выборе должна быть одна.
     */
    @Test
    void collapsesDuplicateOptionValues() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        MechanicChoice choice = new MechanicChoice();
        choice.setKey("weapon");
        choice.setType(ChoiceType.WEAPON);
        choice.setOptions(List.of(
                new ChoiceOption("MATERIAL_MELEE", "Воинское рукопашное"),
                new ChoiceOption("MATERIAL_RANGED", "Воинское дальнобойное")));
        mechanics.setChoices(List.of(choice));
        feat.setMechanics(mechanics);

        JsonNode options = json(feat).get("featData").get("choices").get(0).get("options");
        assertEquals(1, options.size());
        assertEquals("martial", options.get(0).get("value").asText());
    }

    /** Боеприпас не должен становиться оружием: отсекается только суффикс источника. */
    @Test
    void doesNotTurnAmmunitionIntoWeapon() {
        assertEquals("sling-bullet-phb", optionValue(ChoiceType.WEAPON, "sling-bullet-phb"));
    }

    /** Слаги сайта, которые не сводятся правилом: волынка и игральные карты. */
    @Test
    void mapsToolUrlAliases() {
        assertEquals("bagpipes", optionValue(ChoiceType.TOOL, "bagpipe-phb"));
        assertEquals("playing-card-set", optionValue(ChoiceType.TOOL, "playing-cards-phb"));
    }

    /**
     * Обобщённые «инструменты ремесленника» и «игровой набор» ключа у листа не имеют:
     * ключ, который лист не знает, он молча выбросит вместе с владением.
     */
    @Test
    void keepsGenericToolGroupsAsReference() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        ProficiencyGrant grant = new ProficiencyGrant();
        grant.setTools(List.of(new EntityRef("artisan-s-tools-phb", "Инструменты ремесленников")));
        mechanics.setProficiencies(grant);
        feat.setMechanics(mechanics);

        JsonNode json = json(feat);
        assertFalse(json.has("featData"));
        assertEquals("artisan-s-tools-phb",
                json.get("mechanics").get("proficiencies").get("tools").get(0).get("url").asText());
    }

    /** «Отмеченный драконом»: заклинания, которые черта даёт знать без выбора. */
    @Test
    void mapsGrantedSpells() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        SpellGrant spells = new SpellGrant();
        spells.setSpells(List.of(
                new GrantedSpellRef("thaumaturgy-phb", "Чудотворство", null),
                new GrantedSpellRef("command-phb", "Приказ", null)));
        spells.setSpellcastingAbility(Ability.CHARISMA);
        spells.setAlwaysPrepared(Boolean.TRUE);
        mechanics.setSpells(spells);
        feat.setMechanics(mechanics);

        JsonNode featData = json(feat).get("featData");
        JsonNode granted = featData.get("grantedSpells");

        // Порядок редактора сохраняется: игрок видит заклинания так, как их перечислили
        assertEquals(2, granted.size());
        assertEquals("Чудотворство", granted.get(0).get("name").asText());
        assertEquals("thaumaturgy-phb", granted.get(0).get("spellId").asText());
        assertEquals("command-phb", granted.get(1).get("spellId").asText());
        assertEquals("charisma", featData.get("spellcastingAbility").asText());
        assertTrue(featData.get("grantedSpellsAlwaysPrepared").asBoolean());
    }

    /**
     * Редактор черты сохраняет только ссылку на заклинание, без названия: название берётся
     * из справочника, иначе в книге заклинаний оказался бы слаг.
     */
    @Test
    void takesGrantedSpellNameFromCatalog() {
        when(spellRepository.findAllShortByUrlIn(any()))
                .thenReturn(List.of(spell("fly-phb", "Полёт")));

        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        SpellGrant spells = new SpellGrant();
        spells.setSpells(List.of(new GrantedSpellRef("fly-phb", null, null)));
        mechanics.setSpells(spells);
        feat.setMechanics(mechanics);

        JsonNode granted = json(feat).get("featData").get("grantedSpells").get(0);
        assertEquals("Полёт", granted.get("name").asText());
        assertEquals("fly-phb", granted.get("spellId").asText());
    }

    /** Заклинания, которого в справочнике уже нет, хватает снимка имени — иначе слаг. */
    @Test
    void fallsBackToSnapshotNameForMissingSpell() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        SpellGrant spells = new SpellGrant();
        spells.setSpells(List.of(
                new GrantedSpellRef("removed-spell-hb", "Забытое заклинание", null),
                new GrantedSpellRef("nameless-hb", null, null)));
        mechanics.setSpells(spells);
        feat.setMechanics(mechanics);

        JsonNode granted = json(feat).get("featData").get("grantedSpells");
        assertEquals("Забытое заклинание", granted.get(0).get("name").asText());
        assertEquals("nameless-hb", granted.get(1).get("name").asText());
    }

    /**
     * Уровень выдачи доезжает до листа: у метки дракона «Малое восстановление» приходит
     * на третьем уровне, и без уровня лист выдал бы его вместе с «Лечением ран».
     */
    @Test
    void mapsRequiredLevelOfGrantedSpell() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        SpellGrant spells = new SpellGrant();
        spells.setSpells(List.of(
                new GrantedSpellRef("cure-wounds-phb", "Лечение ран", null),
                new GrantedSpellRef("lesser-restoration-phb", "Малое восстановление", 3)));
        mechanics.setSpells(spells);
        feat.setMechanics(mechanics);

        JsonNode granted = json(feat).get("featData").get("grantedSpells");

        // Доступное сразу поля не несёт — пустое поле лист читает как «с момента взятия»
        assertFalse(granted.get(0).has("requiredLevel"));
        assertEquals(3, granted.get(1).get("requiredLevel").asInt());
    }

    /**
     * «Заклинания метки» — не выдача: лист лишь добавляет их в список класса, и подготовку
     * с ячейкой персонаж тратит сам. Поэтому отдельным полем от {@code grantedSpells}.
     */
    @Test
    void mapsSpellListSeparatelyFromGrant() {
        when(spellRepository.findAllShortByUrlIn(any()))
                .thenReturn(List.of(spell("identify-phb", "Опознание")));

        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        SpellListExpansion expansion = new SpellListExpansion();
        expansion.setGroups(List.of(spellListGroup(null, null, "identify-phb")));
        expansion.setRequiresSpellcasting(Boolean.TRUE);
        mechanics.setSpellList(expansion);
        feat.setMechanics(mechanics);

        JsonNode group = json(feat).get("featData").get("spellList").get("groups").get(0);

        // Черта ничего не выдаёт — только расширяет список
        assertFalse(json(feat).get("featData").has("grantedSpells"));
        assertEquals("Опознание", group.get("spells").get(0).get("name").asText());
        assertEquals("identify-phb", group.get("spells").get(0).get("spellId").asText());
        assertTrue(json(feat).get("featData").get("spellList").get("requiresSpellcasting").asBoolean());
        // Круг не дублируется: лист берёт его из записи компендиума
        assertFalse(group.get("spells").get(0).has("level"));
        // Список без уровня и количества полей не несёт — он доступен сразу и целиком
        assertFalse(group.has("requiredLevel"));
        assertFalse(group.has("count"));
    }

    /**
     * Списки открываются ступенями, и каждая едет своим уровнем и количеством: без
     * разбивки лист открыл бы всю таблицу на первом уровне.
     */
    @Test
    void mapsSpellListGroupsWithLevelAndCount() {
        when(spellRepository.findAllShortByUrlIn(any()))
                .thenReturn(List.of(spell("identify-phb", "Опознание"),
                        spell("fireball-phb", "Огненный шар")));

        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        SpellListExpansion expansion = new SpellListExpansion();
        expansion.setGroups(List.of(
                spellListGroup(null, null, "identify-phb"),
                spellListGroup(5, "@prof", "fireball-phb")));
        mechanics.setSpellList(expansion);
        feat.setMechanics(mechanics);

        JsonNode groups = json(feat).get("featData").get("spellList").get("groups");

        assertEquals(2, groups.size());
        assertFalse(groups.get(0).has("requiredLevel"));
        assertEquals(5, groups.get(1).get("requiredLevel").asInt());
        assertEquals("@prof", groups.get(1).get("count").asText());
        assertEquals("fireball-phb", groups.get(1).get("spells").get(0).get("spellId").asText());
    }

    /**
     * Запись, сохранённая до появления списков, читается как один список — доступен сразу
     * и целиком. Иначе у неё пропала бы вся таблица.
     */
    @Test
    void readsFlatSpellListAsSingleGroup() {
        when(spellRepository.findAllShortByUrlIn(any()))
                .thenReturn(List.of(spell("identify-phb", "Опознание")));

        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        SpellListExpansion expansion = new SpellListExpansion();
        expansion.setSpells(List.of(new EntityRef("identify-phb", null)));
        mechanics.setSpellList(expansion);
        feat.setMechanics(mechanics);

        JsonNode groups = json(feat).get("featData").get("spellList").get("groups");

        assertEquals(1, groups.size());
        assertFalse(groups.get(0).has("requiredLevel"));
        assertEquals("identify-phb", groups.get(0).get("spells").get(0).get("spellId").asText());
    }

    /** Список, все ссылки которого битые, выбрасывается: пустая ступень читалась бы как «тут пусто». */
    @Test
    void dropsSpellListGroupWithoutUsableSpells() {
        when(spellRepository.findAllShortByUrlIn(any()))
                .thenReturn(List.of(spell("identify-phb", "Опознание")));

        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        SpellListExpansion expansion = new SpellListExpansion();
        expansion.setGroups(List.of(
                spellListGroup(null, null, "identify-phb"),
                spellListGroup(5, null, (String) null)));
        mechanics.setSpellList(expansion);
        feat.setMechanics(mechanics);

        JsonNode groups = json(feat).get("featData").get("spellList").get("groups");

        assertEquals(1, groups.size());
        assertFalse(groups.get(0).has("requiredLevel"));
    }

    /**
     * Перечисленные заклинания выбора подписываются из справочника: снимок в записи мог
     * устареть, а игрок должен видеть нынешнее название. Записи, которой в справочнике нет,
     * остаётся снимок — само значение не теряется, это url записи компендиума.
     */
    @Test
    void resolvesSpellOptionNamesFromCatalog() {
        when(spellRepository.findAllShortByUrlIn(Set.of("fireball-phb", "lost-spell")))
                .thenReturn(List.of(spell("fireball-phb", "Огненный шар")));

        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        MechanicChoice choice = new MechanicChoice();
        choice.setKey("arcanum");
        choice.setType(ChoiceType.SPELL);
        choice.setOptions(List.of(new ChoiceOption("fireball-phb", "Старое имя"),
                new ChoiceOption("lost-spell", "Снимок")));
        mechanics.setChoices(List.of(choice));
        feat.setMechanics(mechanics);

        JsonNode options = json(feat).get("featData").get("choices").get(0).get("options");

        assertEquals("fireball-phb", options.get(0).get("value").asText());
        assertEquals("Огненный шар", options.get(0).get("name").asText());
        assertEquals("lost-spell", options.get(1).get("value").asText());
        assertEquals("Снимок", options.get(1).get("name").asText());
    }


    /**
     * Список класса уезжает правилом, а не перечнем: заклинания собирает потребитель по
     * своему компендиуму, поэтому добавленное мастером заклинание попадёт в выдачу без
     * правки записи.
     */
    @Test
    void mapsGrantedClassSpellsAsRule() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        SpellGrant spells = new SpellGrant();
        spells.setClassLists(List.of(classList(10, null, 3, null, "druid-phb")));
        mechanics.setSpells(spells);
        feat.setMechanics(mechanics);

        JsonNode granted = json(feat).get("featData").get("grantedClassSpells").get(0);

        assertEquals("druid", granted.get("classKeys").get(0).asText());
        assertEquals(3, granted.get("maxLevel").asInt());
        assertEquals(10, granted.get("requiredLevel").asInt());
        assertFalse(granted.has("level"));
        assertFalse(granted.has("fromSlots"));
    }

    /** Отметка «по ячейкам» едет флагом: круг режет лист, у которого ячейки посчитаны. */
    @Test
    void keepsFromSlotsFlagOnGrantedClassSpells() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        SpellGrant spells = new SpellGrant();
        spells.setClassLists(List.of(classList(null, null, null, Boolean.TRUE, "druid-phb")));
        mechanics.setSpells(spells);
        feat.setMechanics(mechanics);

        JsonNode granted = json(feat).get("featData").get("grantedClassSpells").get(0);

        assertTrue(granted.get("fromSlots").asBoolean());
    }

    /**
     * Класс без канонического ключа (хоумбрю) в выгрузку не попадает: сверять
     * {@code spell.classKeys} в мире будет не с чем, а пустой список читался бы как «весь
     * компендиум».
     */
    @Test
    void skipsGrantedClassSpellsWithoutCanonicalKey() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        SpellGrant spells = new SpellGrant();
        spells.setClassLists(List.of(classList(null, null, null, null, "homebrew-class")));
        mechanics.setSpells(spells);
        feat.setMechanics(mechanics);

        assertFalse(json(feat).has("featData"));
    }

    private static ClassSpellListGrant classList(Integer requiredLevel, Integer level, Integer maxLevel,
                                                 Boolean fromSlots, String... classUrls) {
        ClassSpellListGrant classList = new ClassSpellListGrant();
        classList.setRequiredLevel(requiredLevel);
        classList.setLevel(level);
        classList.setMaxLevel(maxLevel);
        classList.setMaxLevelFromSlots(fromSlots);
        classList.setClasses(Arrays.stream(classUrls).map(url -> new EntityRef(url, null)).toList());
        return classList;
    }

    private static SpellListGroup spellListGroup(Integer requiredLevel, String count, String... urls) {
        SpellListGroup group = new SpellListGroup();
        group.setRequiredLevel(requiredLevel);
        group.setCount(count);
        group.setSpells(Arrays.stream(urls).map(url -> new EntityRef(url, null)).toList());
        return group;
    }

    /**
     * Заклинательная характеристика без самих заклинаний блок даров не создаёт: применять
     * в нём нечего.
     */
    @Test
    void omitsFeatDataForSpellcastingAbilityAlone() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        SpellGrant spells = new SpellGrant();
        spells.setSpellcastingAbility(Ability.INTELLIGENCE);
        mechanics.setSpells(spells);
        feat.setMechanics(mechanics);

        assertFalse(json(feat).has("featData"));
    }

    /** Ссылка без url не выдаётся: без неё потребитель заклинание всё равно не найдёт. */
    @Test
    void skipsGrantedSpellWithoutUrl() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        SpellGrant spells = new SpellGrant();
        spells.setSpells(List.of(new GrantedSpellRef(null, "Забытое заклинание", null)));
        mechanics.setSpells(spells);
        feat.setMechanics(mechanics);

        assertFalse(json(feat).has("featData"));
    }

    /**
     * «Посвящённый в магию»: сперва выбирается список класса, и только потом заговоры из
     * него — пул сужается ответом игрока, а не объединением трёх списков.
     */
    @Test
    void mapsSpellFilterWithClassChoice() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();

        MechanicChoice list = new MechanicChoice();
        list.setKey("spell-list");
        list.setType(ChoiceType.SPELL_LIST);
        list.setOptions(List.of(new ChoiceOption("wizard-phb", "Волшебник")));

        SpellFilter filter = new SpellFilter();
        filter.setLevel(0);
        filter.setClassesFromChoiceKey("spell-list");

        MechanicChoice cantrip = new MechanicChoice();
        cantrip.setKey("cantrip");
        cantrip.setType(ChoiceType.CANTRIP);
        cantrip.setCount(2);
        cantrip.setSpellFilter(filter);

        mechanics.setChoices(List.of(list, cantrip));
        feat.setMechanics(mechanics);

        JsonNode choices = json(feat).get("featData").get("choices");
        assertEquals("wizard", choices.get(0).get("options").get(0).get("value").asText());
        assertEquals("spell-list",
                choices.get(1).get("spellFilter").get("classesFromChoiceKey").asText());
    }

    /** Класс фильтра едет и ссылкой (для показа), и ключом (для сборки пула). */
    @Test
    void mapsSpellFilterClassKeys() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();

        SpellFilter filter = new SpellFilter();
        filter.setClasses(List.of(new EntityRef("wizard-phb", "Волшебник")));

        MechanicChoice choice = new MechanicChoice();
        choice.setKey("spell");
        choice.setType(ChoiceType.SPELL);
        choice.setSpellFilter(filter);
        mechanics.setChoices(List.of(choice));
        feat.setMechanics(mechanics);

        JsonNode spellFilter = json(feat).get("featData").get("choices").get(0).get("spellFilter");
        assertEquals("wizard-phb", spellFilter.get("classes").get(0).get("url").asText());
        assertEquals("wizard", spellFilter.get("classKeys").get(0).asText());
    }

    /**
     * Фильтр, у которого задан только ключ выбора, не схлопывается в пустой: иначе
     * «Посвящённый в магию» потерял бы связь между списком и заговорами.
     */
    @Test
    void keepsSpellFilterWithOnlyChoiceKey() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();

        SpellFilter filter = new SpellFilter();
        filter.setClassesFromChoiceKey("spell-list");

        MechanicChoice choice = new MechanicChoice();
        choice.setKey("cantrip");
        choice.setType(ChoiceType.CANTRIP);
        choice.setSpellFilter(filter);
        mechanics.setChoices(List.of(choice));
        feat.setMechanics(mechanics);

        assertEquals("spell-list", json(feat).get("featData").get("choices").get(0)
                .get("spellFilter").get("classesFromChoiceKey").asText());
    }

    /**
     * Требование, которое не разобралось в поля, доезжает строкой: в описании черты его
     * нет, и без этого поля оно пропадало бы совсем.
     */
    @Test
    void mapsPrerequisiteTextFromBookString() {
        Feat feat = baseFeat();
        feat.setPrerequisite("Эльф или полуэльф");

        assertEquals("Эльф или полуэльф",
                json(feat).get("featData").get("prerequisite").get("text").asText());
    }

    /**
     * Строка требования размечена так же, как описание: маркеры раскрываются, иначе в
     * компендиум уехали бы фигурные скобки.
     */
    @Test
    void expandsMarkupInPrerequisiteText() {
        Feat feat = baseFeat();
        feat.setPrerequisite("{@class Волшебник|url:wizard-phb} 4 уровня");

        String text = json(feat).get("featData").get("prerequisite").get("text").asText();
        assertFalse(text.contains("{@"));
        assertTrue(text.contains("Волшебник"));
    }

    /** Разобранное непроверяемое условие важнее книжной строки: та повторяла бы поля. */
    @Test
    void prefersCustomConditionOverBookString() {
        Feat feat = baseFeat();
        feat.setPrerequisite("13 уровень, превращение в лича");
        FeatPrerequisite prerequisite = new FeatPrerequisite();
        prerequisite.setMinCharacterLevel(13);
        prerequisite.setCustom("превращение в лича");
        feat.setPrerequisiteDetails(prerequisite);

        JsonNode json = json(feat).get("featData").get("prerequisite");
        assertEquals(13, json.get("minLevel").asInt());
        assertEquals("превращение в лича", json.get("text").asText());
    }

    /** Предел повышения: 20 у черт, 30 у эпических даров — тем они и отличаются. */
    @Test
    void mapsAbilityScoreIncreaseLimit() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setAbilityBonuses(List.of(
                abilityBonus(List.of(Ability.STRENGTH, Ability.DEXTERITY), 1, 30, 1)));
        feat.setMechanics(mechanics);

        JsonNode increase = json(feat).get("featData").get("abilityScoreIncrease");
        assertEquals(30, increase.get("upto").asInt());
        assertEquals(1, increase.get("choice").get("amount").asInt());
    }

    /**
     * «Крепкий»: характеристика одна и выбирать нечего — повышение едет готовой прибавкой,
     * и лист ставит его сам. Выбором оно осталось бы подсказкой в сводке даров.
     */
    @Test
    void mapsAbilityScoreIncreaseWithoutChoiceAsFixed() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setAbilityBonuses(List.of(
                abilityBonus(List.of(Ability.CONSTITUTION), 1, 20, 1)));
        HitPointsModifier hitPoints = new HitPointsModifier();
        hitPoints.setPerAcquisitionLevel(2);
        hitPoints.setPerLevelAfterAcquisition(2);
        SheetModifiers modifiers = new SheetModifiers();
        modifiers.setHitPoints(hitPoints);
        mechanics.setModifiers(modifiers);
        feat.setMechanics(mechanics);

        JsonNode json = json(feat);
        JsonNode increase = json.get("featData").get("abilityScoreIncrease");
        assertEquals(1, increase.get("fixed").get("constitution").asInt());
        assertFalse(increase.has("choice"));
        assertEquals(20, increase.get("upto").asInt());

        JsonNode hp = json.get("featData").get("modifiers").get("hitPoints");
        assertEquals(2, hp.get("perAcquisitionLevel").asInt());
        assertEquals(2, hp.get("perLevelAfterAcquisition").asInt());
        // Единственный вариант целиком уехал в featData — копией в mechanics он не едет
        assertFalse(json.has("mechanics"));
    }

    /**
     * «Устойчивый»: выбор спасброска даёт владение им, а повышение привязано к тому же
     * выбору — характеристика берётся оттуда, а не спрашивается второй раз.
     */
    @Test
    void bindsAbilityScoreIncreaseToSavingThrowChoice() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();

        MechanicChoice choice = new MechanicChoice();
        choice.setKey("saving-throw");
        choice.setType(ChoiceType.SAVING_THROW);
        choice.setOnlyIfNotProficient(Boolean.TRUE);
        mechanics.setChoices(List.of(choice));

        AbilityBonus bonus = new AbilityBonus();
        bonus.setBonus(1);
        bonus.setUpto(20);
        bonus.setFromChoiceKey("saving-throw");
        mechanics.setAbilityBonuses(List.of(bonus));
        feat.setMechanics(mechanics);

        JsonNode featData = json(feat).get("featData");
        assertEquals("savingThrow", featData.get("choices").get(0).get("type").asText());
        JsonNode increase = featData.get("abilityScoreIncrease");
        assertEquals("saving-throw", increase.get("fromChoiceKey").asText());
        // Характеристика придёт из выбора — готовой прибавки здесь быть не может
        assertFalse(increase.has("fixed"));
    }

    /**
     * Привязки в записи нет — берётся единственный выбор спасброска: без неё лист повышение
     * не применяет вовсе, и у черт, сохранённых до появления поля, оно молча не работало бы.
     */
    @Test
    void bindsAbilityScoreIncreaseToTheOnlySavingThrowChoiceWithoutExplicitKey() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();

        MechanicChoice choice = new MechanicChoice();
        choice.setKey("saving-throw");
        choice.setType(ChoiceType.SAVING_THROW);
        mechanics.setChoices(List.of(choice));
        mechanics.setAbilityBonuses(List.of(abilityBonus(List.of(), 1, 20, 1)));
        feat.setMechanics(mechanics);

        assertEquals("saving-throw", json(feat).get("featData")
                .get("abilityScoreIncrease").get("fromChoiceKey").asText());
    }

    /** Два выбора спасброска — угадывать нечего: привязка не подставляется. */
    @Test
    void doesNotGuessAbilityScoreIncreaseBindingAmongSeveralSavingThrowChoices() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setChoices(List.of(
                choice("first", ChoiceType.SAVING_THROW),
                choice("second", ChoiceType.SAVING_THROW)));
        mechanics.setAbilityBonuses(List.of(abilityBonus(List.of(), 1, 20, 1)));
        feat.setMechanics(mechanics);

        assertFalse(json(feat).get("featData").get("abilityScoreIncrease").has("fromChoiceKey"));
    }

    /**
     * У повышения есть свой список характеристик — значит, оно описывает собственный выбор,
     * и привязка подменила бы его ответом на чужой вопрос.
     */
    @Test
    void doesNotBindAbilityScoreIncreaseThatPicksItsOwnAbilities() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setChoices(List.of(choice("saving-throw", ChoiceType.SAVING_THROW)));
        mechanics.setAbilityBonuses(List.of(
                abilityBonus(List.of(Ability.STRENGTH, Ability.DEXTERITY), 1, 20, 1)));
        feat.setMechanics(mechanics);

        JsonNode increase = json(feat).get("featData").get("abilityScoreIncrease");
        assertFalse(increase.has("fromChoiceKey"));
        assertEquals(2, increase.get("choice").get("from").size());
    }

    /**
     * Выбор характеристики с повышением не связан: {@code ABILITY} — это характеристика
     * вне повышения и спасбросков, и привязывать к ней повышение было бы догадкой.
     */
    @Test
    void doesNotBindAbilityScoreIncreaseToAbilityChoice() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setChoices(List.of(choice("ability", ChoiceType.ABILITY)));
        mechanics.setAbilityBonuses(List.of(abilityBonus(List.of(), 1, 20, 1)));
        feat.setMechanics(mechanics);

        assertFalse(json(feat).get("featData").get("abilityScoreIncrease").has("fromChoiceKey"));
    }

    /**
     * «Умелый»: три штуки вперемешку из навыков и инструментов. Куда лечь выбранному,
     * решает сам справочник, поэтому едет весь набор видов.
     */
    @Test
    void mapsMixedChoiceTypes() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();

        MechanicChoice choice = new MechanicChoice();
        choice.setKey("skill-or-tool");
        choice.setType(ChoiceType.SKILL);
        choice.setTypes(List.of(ChoiceType.SKILL, ChoiceType.TOOL));
        choice.setCount(3);
        choice.setOptions(List.of(
                new ChoiceOption("SLEIGHT_OF_HAND", "Ловкость рук"),
                new ChoiceOption("thieves-tools-phb", "Воровские инструменты")));
        mechanics.setChoices(List.of(choice));
        feat.setMechanics(mechanics);

        JsonNode json = json(feat).get("featData").get("choices").get(0);
        // В type — первый вид набора: запись остаётся читаемой и без знания о смешивании
        assertEquals("skill", json.get("type").asText());
        assertEquals(List.of("skill", "tool"), names(json.get("types")));
        assertEquals(3, json.get("count").asInt());
        // Каждое значение переведено своим словарём
        assertEquals("sleightOfHand", json.get("options").get(0).get("value").asText());
        assertEquals("thieves-tools", json.get("options").get(1).get("value").asText());
    }

    /**
     * Смешивать можно только виды со справочником правил: оружие в наборе разложить по
     * значению нечем, и набор сворачивается до основного вида.
     */
    @Test
    void collapsesMixedChoiceWithVocabularylessType() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();

        MechanicChoice choice = new MechanicChoice();
        choice.setKey("skill-or-weapon");
        choice.setType(ChoiceType.SKILL);
        choice.setTypes(List.of(ChoiceType.SKILL, ChoiceType.WEAPON));
        mechanics.setChoices(List.of(choice));
        feat.setMechanics(mechanics);

        JsonNode json = json(feat).get("featData").get("choices").get(0);
        assertEquals("skill", json.get("type").asText());
        assertFalse(json.has("types"));
    }

    /** Один вид описан полем type — списком он повторял бы его без нужды. */
    @Test
    void omitsTypesForSingleTypeChoice() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();

        MechanicChoice choice = new MechanicChoice();
        choice.setKey("skill");
        choice.setTypes(List.of(ChoiceType.SKILL));
        mechanics.setChoices(List.of(choice));
        feat.setMechanics(mechanics);

        JsonNode json = json(feat).get("featData").get("choices").get(0);
        assertEquals("skill", json.get("type").asText());
        assertFalse(json.has("types"));
    }

    /**
     * «Мастер оружия»: владение конкретным видом оружия и оружейный приём. На листе это
     * разные списки, и приём не подмножество владения.
     */
    @Test
    void mapsWeaponsAndWeaponMasteries() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        ProficiencyGrant grant = new ProficiencyGrant();
        grant.setWeaponCategories(Set.of(WeaponCategory.SIMPLE_MELEE));
        grant.setWeapons(List.of(new EntityRef("longsword-phb", "Длинный меч")));
        grant.setWeaponMasteries(List.of(new EntityRef("rapier-phb", "Рапира")));
        mechanics.setProficiencies(grant);
        feat.setMechanics(mechanics);

        JsonNode featData = json(feat).get("featData");
        // Категория и конкретный вид — один список: потребитель принимает и то, и другое
        assertEquals(List.of("simple", "longsword"), names(featData.get("weaponProficiencies")));
        assertEquals(List.of("rapier"), names(featData.get("weaponMasteries")));
    }

    /** Оружие с приёмом на выбор — тем же ключом вида оружия, что и владение. */
    @Test
    void mapsWeaponMasteryChoiceValue() {
        assertEquals("longsword", optionValue(ChoiceType.WEAPON_MASTERY, "longsword-phb"));
    }

    /**
     * «Тактический мастер»: приёмы без привязки к оружию. У потребителя это свой список
     * владений, и ключи там — сами приёмы, а не виды оружия.
     */
    @Test
    void mapsMasteryProperties() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        ProficiencyGrant grant = new ProficiencyGrant();
        grant.setMasteryProperties(Set.of(Mastery.PUSH));
        mechanics.setProficiencies(grant);
        feat.setMechanics(mechanics);

        JsonNode featData = json(feat).get("featData");
        assertEquals(List.of("push"), names(featData.get("masteryProperties")));
        // Оружия за приёмом нет: списком видов оружия он не записывается
        assertFalse(featData.has("weaponMasteries"));
    }

    /** Оружейный приём на выбор — ключом самого приёма, а не вида оружия. */
    @Test
    void mapsMasteryPropertyChoiceValue() {
        assertEquals("slow", optionValue(ChoiceType.MASTERY_PROPERTY, "SLOW"));
        assertEquals("topple", optionValue(ChoiceType.MASTERY_PROPERTY, "TOPPLE"));
    }

    /**
     * Приём, которого в справочнике нет, из вариантов выбрасывается: у листа свой полный
     * справочник из восьми, и пустой список он раскроет целиком — это лучше, чем положить
     * во владения строку, которой в справочнике нет.
     */
    @Test
    void dropsUnknownMasteryPropertyChoiceValue() {
        assertNull(optionValue(ChoiceType.MASTERY_PROPERTY, "WHIRLWIND"));
    }

    /** Доспехи как вид выбора — категориями справочника листа. */
    @Test
    void mapsArmorChoiceValue() {
        assertEquals("medium", optionValue(ChoiceType.ARMOR, "MEDIUM"));
        assertEquals("shield", optionValue(ChoiceType.ARMOR, "SHIELD"));
    }

    /** Владение спасбросками без выбора: у листа это свой список, не характеристики. */
    @Test
    void mapsFixedSavingThrowProficiencies() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        ProficiencyGrant grant = new ProficiencyGrant();
        grant.setSavingThrows(Set.of(Ability.WISDOM));
        mechanics.setProficiencies(grant);
        feat.setMechanics(mechanics);

        assertEquals(List.of("wisdom"),
                names(json(feat).get("featData").get("savingThrowProficiencies")));
    }

    /**
     * Вид оружия, которого в справочнике листа нет, остаётся ссылкой в механике: применить
     * его нечем, но показать нужно — как и незнакомый инструмент.
     */
    @Test
    void keepsUnknownWeaponAsReference() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        ProficiencyGrant grant = new ProficiencyGrant();
        grant.setWeapons(List.of(new EntityRef("astral-blade-hb", "Астральный клинок")));
        grant.setWeaponMasteries(List.of(new EntityRef("astral-blade-hb", "Астральный клинок")));
        mechanics.setProficiencies(grant);
        feat.setMechanics(mechanics);

        JsonNode json = json(feat);
        assertFalse(json.has("featData"));
        JsonNode proficiencies = json.get("mechanics").get("proficiencies");
        assertEquals("astral-blade-hb", proficiencies.get("weapons").get(0).get("url").asText());
        assertEquals("astral-blade-hb",
                proficiencies.get("weaponMasteries").get(0).get("url").asText());
    }

    /**
     * «Удачливый»: очки удачи по бонусу мастерства, откат на продолжительном отдыхе.
     * Максимум — формулой: он обязан расти вместе с бонусом мастерства.
     */
    @Test
    void mapsCounters() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setCounters(List.of(counter("luck-points", "@prof", ResourceRecovery.LONG_REST)));
        feat.setMechanics(mechanics);

        JsonNode counter = json(feat).get("featData").get("counters").get(0);
        assertEquals("luck-points", counter.get("key").asText());
        assertEquals("Очки удачи", counter.get("name").asText());
        assertEquals("Удача", counter.get("shortName").asText());
        assertEquals("@prof", counter.get("max").asText());
        assertEquals("long", counter.get("recovery").asText());
    }

    /** Откат коротким отдыхом — «Целитель» и прочие ресурсы, что возвращаются между боями. */
    @Test
    void mapsShortRestCounter() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setCounters(List.of(counter("uses", "1", ResourceRecovery.SHORT_REST)));
        feat.setMechanics(mechanics);

        assertEquals("short",
                json(feat).get("featData").get("counters").get(0).get("recovery").asText());
    }

    /**
     * Один заряд коротким отдыхом, все — продолжительным: у такого отката своё значение
     * словаря, иначе он читался бы как полный откат коротким.
     */
    @Test
    void mapsShortRestOneCounter() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setCounters(List.of(counter("uses", "2", ResourceRecovery.SHORT_REST_ONE)));
        feat.setMechanics(mechanics);

        assertEquals("short-one",
                json(feat).get("featData").get("counters").get(0).get("recovery").asText());
    }

    /** Нижняя граница максимума едет вместе с формулой: с модификатором +0 ресурс не пропадёт. */
    @Test
    void mapsCounterMinimum() {
        Feat feat = baseFeat();
        ResourceCounter counter = counter("uses", "@mod.cha", ResourceRecovery.LONG_REST);
        counter.setMin(1);
        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setCounters(List.of(counter));
        feat.setMechanics(mechanics);

        assertEquals(1, json(feat).get("featData").get("counters").get(0).get("min").asInt());
    }

    /** Откат не задан — продолжительный отдых: короткий проставляют явно. */
    @Test
    void defaultsCounterRecoveryToLongRest() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setCounters(List.of(counter("uses", "1", null)));
        feat.setMechanics(mechanics);

        assertEquals("long",
                json(feat).get("featData").get("counters").get(0).get("recovery").asText());
    }

    /** Ресурс без ключа или без формулы максимума не едет: счётчик, всегда пустой, только мешает. */
    @Test
    void skipsIncompleteCounters() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setCounters(List.of(counter("uses", null, ResourceRecovery.LONG_REST),
                counter(null, "@prof", ResourceRecovery.LONG_REST)));
        feat.setMechanics(mechanics);

        assertFalse(json(feat).has("featData"));
    }

    /** Числовая прибавка к инициативе — «Бдительный» издания 2014 года. */
    @Test
    void mapsFlatInitiativeBonus() {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        SheetModifiers modifiers = new SheetModifiers();
        modifiers.setInitiativeBonus(5);
        mechanics.setModifiers(modifiers);
        feat.setMechanics(mechanics);

        assertEquals(5, json(feat).get("featData").get("modifiers").get("initiativeBonus").asInt());
    }

    /**
     * Значение варианта выбора данного типа — через выгрузку целой черты; {@code null},
     * если вариант из выгрузки выпал.
     */
    private String optionValue(ChoiceType type, String raw) {
        Feat feat = baseFeat();
        FeatMechanics mechanics = new FeatMechanics();
        MechanicChoice choice = new MechanicChoice();
        choice.setKey("choice");
        choice.setType(type);
        choice.setOptions(List.of(new ChoiceOption(raw, "Подпись")));
        mechanics.setChoices(List.of(choice));
        feat.setMechanics(mechanics);

        JsonNode options = json(feat).get("featData").get("choices").get(0).get("options");
        return options == null ? null : options.get(0).get("value").asText();
    }

    /** Заклинание справочника: маппер ходит за названием именно по url. */
    private Spell spell(String url, String name) {
        Spell spell = new Spell();
        spell.setUrl(url);
        spell.setName(name);
        return spell;
    }

    private List<String> names(JsonNode array) {
        return objectMapper.convertValue(array, objectMapper.getTypeFactory()
                .constructCollectionType(List.class, String.class));
    }

    private MechanicChoice choice(String key, ChoiceType type) {
        MechanicChoice choice = new MechanicChoice();
        choice.setKey(key);
        choice.setType(type);
        return choice;
    }

    private ResourceCounter counter(String key, String max, ResourceRecovery recovery) {
        ResourceCounter counter = new ResourceCounter();
        counter.setKey(key);
        counter.setName("Очки удачи");
        counter.setShortName("Удача");
        counter.setMax(max);
        counter.setRecovery(recovery);
        return counter;
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

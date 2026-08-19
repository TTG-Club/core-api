package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Language;
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
import club.ttg.dnd5.domain.common.model.mechanics.SpellFilter;
import club.ttg.dnd5.domain.common.model.mechanics.SpellGrant;
import club.ttg.dnd5.domain.feat.model.prerequisite.AbilityRequirement;
import club.ttg.dnd5.domain.feat.model.prerequisite.ClassFeatureRequirement;
import club.ttg.dnd5.domain.feat.model.prerequisite.FeatPrerequisite;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

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
            markupConverter, new VttgFeatMechanicsMapper(markupConverter, spellRepository));

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
                new EntityRef("thaumaturgy-phb", "Чудотворство"),
                new EntityRef("command-phb", "Приказ")));
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
        spells.setSpells(List.of(new EntityRef("fly-phb", null)));
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
                new EntityRef("removed-spell-hb", "Забытое заклинание"),
                new EntityRef("nameless-hb", null)));
        mechanics.setSpells(spells);
        feat.setMechanics(mechanics);

        JsonNode granted = json(feat).get("featData").get("grantedSpells");
        assertEquals("Забытое заклинание", granted.get(0).get("name").asText());
        assertEquals("nameless-hb", granted.get(1).get("name").asText());
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
        spells.setSpells(List.of(new EntityRef(null, "Забытое заклинание")));
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
                abilityBonus(List.of(Ability.STRENGTH), 1, 30, 1)));
        feat.setMechanics(mechanics);

        JsonNode increase = json(feat).get("featData").get("abilityScoreIncrease");
        assertEquals(30, increase.get("upto").asInt());
        assertEquals(1, increase.get("choice").get("amount").asInt());
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

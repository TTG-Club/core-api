package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgCreature;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgEquipmentItem;
import club.ttg.dnd5.domain.beastiary.model.CreatureAbilities;
import club.ttg.dnd5.domain.beastiary.model.CreatureAbility;
import club.ttg.dnd5.domain.beastiary.model.CreatureArmor;
import club.ttg.dnd5.domain.beastiary.model.CreatureCategory;
import club.ttg.dnd5.domain.beastiary.model.CreatureHit;
import club.ttg.dnd5.domain.beastiary.model.CreatureInitiative;
import club.ttg.dnd5.domain.beastiary.model.CreatureLair;
import club.ttg.dnd5.domain.beastiary.model.CreatureSection;
import club.ttg.dnd5.domain.beastiary.model.CreatureSize;
import club.ttg.dnd5.domain.beastiary.model.CreatureTrait;
import club.ttg.dnd5.domain.beastiary.model.action.AttackType;
import club.ttg.dnd5.domain.beastiary.model.action.CreatureActionEffect;
import club.ttg.dnd5.domain.beastiary.model.action.CreatureAction;
import club.ttg.dnd5.domain.beastiary.model.action.SawingThrow;
import club.ttg.dnd5.domain.beastiary.model.sense.Senses;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Alignment;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.CreatureTreasure;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Habitat;
import club.ttg.dnd5.domain.common.dictionary.RechargeType;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.model.DamagePart;
import club.ttg.dnd5.domain.common.model.EquipmentItem;
import club.ttg.dnd5.domain.beastiary.model.spellcasting.CreatureSpellComponents;
import club.ttg.dnd5.domain.beastiary.model.spellcasting.CreatureSpellGroup;
import club.ttg.dnd5.domain.beastiary.model.spellcasting.CreatureSpellRef;
import club.ttg.dnd5.domain.beastiary.model.spellcasting.CreatureSpellRestKind;
import club.ttg.dnd5.domain.beastiary.model.spellcasting.CreatureSpellUsageMode;
import club.ttg.dnd5.domain.beastiary.model.spellcasting.CreatureSpellcastingBlock;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.model.SpellSchool;
import club.ttg.dnd5.domain.spell.model.enums.MagicSchool;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgSpell;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgSpellUses;
import club.ttg.dnd5.domain.spell.model.AreaOfEffect;
import club.ttg.dnd5.domain.spell.model.enums.AreaOfEffectType;
import club.ttg.dnd5.domain.spell.model.enums.SpellSaveEffect;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VttgCreatureMapperTest {
    private final VttgMarkupConverter markupConverter = new VttgMarkupConverter(new ObjectMapper());
    // Справочник заклинаний отвечает только там, где тест завёл блоки: существо без
    // них в него не ходит.
    private final SpellRepository spellRepository = mock(SpellRepository.class);
    // Справочник предметов молчит: у позиций теста снимок названия заполнен, а
    // дозапрос идёт только за теми, у кого его нет.
    private final VttgCreatureMapper mapper = new VttgCreatureMapper(
            markupConverter,
            new VttgEquipmentMapper(markupConverter, mock(ItemRepository.class)),
            new VttgCreatureSpellcastingMapper(spellRepository, new VttgSpellMapper(
                    markupConverter,
                    new VttgSpellMechanicsExtractor(),
                    new VttgSpellScalingExtractor())));

    @Test
    void mapsCreatureToVttgStructure() {
        Creature creature = new Creature();
        creature.setUrl("test-creature");
        creature.setName("Тестовое существо");
        creature.setEnglish("Test Creature");
        creature.setImageUrl("/s3/bestiary/magistrus/1757076204886-badger.webp");
        creature.setSrdVersion("5.2");
        creature.setExperience(450L);
        creature.setAlignment(Alignment.CHAOTIC_EVIL);
        creature.setDescription("[\"Описание существа\"]");

        CreatureSize size = new CreatureSize();
        size.setValues(List.of(Size.LARGE));
        creature.setSizes(size);
        CreatureCategory category = new CreatureCategory();
        category.setValues(List.of(CreatureType.FIEND));
        category.setText("демон");
        creature.setTypes(category);

        CreatureArmor armor = new CreatureArmor();
        armor.setArmorClass((byte) 15);
        creature.setArmor(armor);
        CreatureHit hit = new CreatureHit();
        hit.setValue((short) 45);
        hit.setCountHitDice((short) 6);
        creature.setHit(hit);

        CreatureAbilities abilities = new CreatureAbilities();
        abilities.setStrength(ability(Ability.STRENGTH, 18, 1));
        abilities.setDexterity(ability(Ability.DEXTERITY, 12, 0));
        abilities.setConstitution(ability(Ability.CONSTITUTION, 16, 0));
        abilities.setIntelligence(ability(Ability.INTELLIGENCE, 8, 0));
        abilities.setWisdom(ability(Ability.WISDOM, 10, 0));
        abilities.setCharisma(ability(Ability.CHARISMA, 14, 0));
        creature.setAbilities(abilities);

        CreatureTrait trait = new CreatureTrait();
        trait.setName("Черта");
        trait.setEnglish("Trait");
        trait.setDescription("[\"Описание черты\"]");
        creature.setTraits(List.of(trait));

        CreatureSection section = new CreatureSection();
        section.setHabitats(List.of(Habitat.FOREST, Habitat.PLANAR_ABYSS, Habitat.PLANAR_FEYWILD));
        creature.setSection(section);

        Senses senses = new Senses();
        senses.setDarkvision((short) 60);
        senses.setBlindsight((short) 10);
        creature.setSenses(senses);

        var result = mapper.toVttg(creature);
        Map<String, Object> system = result.getSystem();

        assertEquals("creature", result.getEntityType());
        assertEquals("creature", result.getType());
        assertTrue(result.getIsSRD());
        assertTrue(result.getIsReadOnly());
        assertToken(result.getToken());
        assertEquals("large", system.get("size"));
        assertEquals("fiend", system.get("type"));
        assertEquals("chaotic-evil", system.get("alignment"));
        assertEquals("2", system.get("challengeRating"));
        assertEquals(List.of("strength"), system.get("savingThrows"));
        assertTrue(result.getDescription().contains("Описание существа"));
        Map<?, ?> mappedTrait = (Map<?, ?>) ((List<?>) system.get("traits")).getFirst();
        assertEquals("Trait", mappedTrait.get("nameEn"));
        assertEquals(List.of("forest", "planar"), system.get("environments"));
        assertFalse(system.containsKey("speed"));
    }

    @Test
    void convertsCreatureActionMarkupToMarkdown() {
        Creature creature = new Creature();
        creature.setUrl("markdown-creature");
        creature.setName("Markdown Creature");
        creature.setDescription("");

        creature.setActions(List.of(action("Action", "[\"{@i Italic} {@b Bold}\"]")));
        creature.setBonusActions(List.of(action("Bonus", "[\"{@glossary creature|url:creature-phb}\"]")));
        creature.setReactions(List.of(action("Reaction", "[\"First paragraph\", \"Second paragraph\"]")));
        creature.setLegendaryActions(List.of(action("Legendary", "[\"Legendary {@i action}\"]")));

        CreatureLair lair = new CreatureLair();
        lair.setName("Lair");
        lair.setDescription("[\"{@b Lair description}\"]");
        lair.setEnding("[\"{@i Lair ending}\"]");
        lair.setEffects(List.of(action("Lair Effect", "[\"{@i Lair effect}\"]")));
        creature.setLair(lair);

        Map<String, Object> system = mapper.toVttg(creature).getSystem();

        assertActionDescription(system, "actions", List.of("*Italic* **Bold**"));
        assertActionDescription(system, "bonusActions", List.of("[creature](https://ttg.club/glossary/creature-phb)"));
        assertActionDescription(system, "reactions", List.of("First paragraph", "Second paragraph"));
        assertLegendaryActionDescription(system, List.of("Legendary *action*"));

        Map<?, ?> lairResult = (Map<?, ?>) system.get("lair");
        assertEquals("**Lair description**", lairResult.get("description"));
        assertEquals("*Lair ending*", lairResult.get("ending"));
        assertActionDescription(lairResult, "effects", List.of("*Lair effect*"));
    }

    @Test
    void exportsSourcePageIdentity() {
        Creature creature = new Creature();
        creature.setUrl("goblin-mm");
        creature.setName("Гоблин");
        creature.setDescription("");

        VttgCreature result = mapper.toVttg(creature);

        assertEquals("bestiary", result.getSrcSection());
        assertEquals("goblin-mm", result.getSrcUrl());
        assertEquals("creatures", result.getSection());
    }

    /**
     * Механику записи не заводили — в выгрузку едет одно описание. Раньше отсюда вынималось
     * всё, до чего дотягивались регулярки: бонус атаки, кости урона, спасбросок, область и
     * состояния. Догадка расходилась с тем, что видит редактор, и запись молча получала
     * механику, которой ей никто не задавал.
     */
    @Test
    void keepsDescriptionOnlyWhenMechanicsNotAuthored() {
        Creature creature = creature("goblin-mm");
        creature.setActions(List.of(action(
                "Укус",
                "[\"*Бросок рукопашной атаки:* +9, досягаемость 15 фт., одна цель. "
                        + "*Попадание:* 12 (2к6 + 5) дробящего урона, и цель отравлена.\"]"
        )));

        Map<?, ?> mapped = firstAction(mapper.toVttg(creature).getSystem());

        assertEquals(
                List.of("*Бросок рукопашной атаки:* +9, досягаемость 15 фт., одна цель. "
                        + "*Попадание:* 12 (2к6 + 5) дробящего урона, и цель отравлена."),
                mapped.get("description")
        );
        assertNull(mapped.get("attackBonus"));
        assertNull(mapped.get("damageParts"));
        assertNull(mapped.get("saveType"));
        assertNull(mapped.get("areaOfEffect"));
        assertNull(mapped.get("reach"));
        assertNull(mapped.get("rangeType"));
        assertNull(mapped.get("activeEffects"));
    }

    /** То же у черты: без заведённой механики она остаётся текстом. */
    @Test
    void keepsTraitDescriptionOnlyWhenMechanicsNotAuthored() {
        Creature creature = creature("aboleth-mm");
        CreatureTrait trait = new CreatureTrait();
        trait.setName("Облако слизи");
        trait.setDescription("[\"*Спасбросок Телосложения:* Сл. 14. *Провал:* цель получает 6 (1к12) урона кислотой.\"]");
        creature.setTraits(List.of(trait));

        Map<?, ?> mapped = (Map<?, ?>) ((List<?>) mapper.toVttg(creature).getSystem().get("traits")).getFirst();

        assertNull(mapped.get("saveType"));
        assertNull(mapped.get("saveDC"));
        assertNull(mapped.get("damageParts"));
        assertNull(mapped.get("areaOfEffect"));
    }

    /** Механику завели в мастерской — она и уезжает в компендиум. */
    @Test
    void exportsAuthoredMechanics() {
        Creature creature = creature("goblin-mm");
        CreatureAction action = new CreatureAction();
        action.setName("Укус");
        action.setDescription("[\"*Рукопашная атака оружием:* +9 к попаданию. *Попадание:* 15 (2к10 + 4) урона.\"]");

        CreatureActionEffect effect = new CreatureActionEffect();
        effect.setAttackType(AttackType.MELEE);
        effect.setAttackBonus(5);
        effect.setReach(10);
        effect.setDamageParts(List.of(damagePart("1к8+3@dmg.piercing")));
        effect.setActiveEffects(List.of(activeEffect("poisoned")));
        action.setEffect(effect);
        creature.setActions(List.of(action));

        Map<?, ?> mapped = firstAction(mapper.toVttg(creature).getSystem());

        assertEquals(5, mapped.get("attackBonus"));
        assertEquals(10, mapped.get("reach"));
        assertEquals("melee", mapped.get("rangeType"));
        assertEquals("ft", mapped.get("distanceUnit"));
        assertDamagePart(mapped, "1к8+3@dmg.piercing", null);
        assertActiveEffect(mapped, "poisoned");
    }

    /** Черта тоже бывает бросаемой — её механика уезжает так же, как у действия. */
    @Test
    void exportsAuthoredTraitMechanics() {
        Creature creature = creature("aboleth-mm");
        CreatureTrait trait = new CreatureTrait();
        trait.setName("Облако слизи");
        trait.setDescription("[\"Находясь под водой, аболет окружён слизью.\"]");

        AreaOfEffect area = new AreaOfEffect();
        area.setType(AreaOfEffectType.EMANATION);
        area.setValue1(5);

        CreatureActionEffect effect = new CreatureActionEffect();
        effect.setSavingThrows(List.of(savingThrow(Ability.CONSTITUTION, 14)));
        effect.setSaveEffect(SpellSaveEffect.NONE);
        effect.setDamageParts(List.of(damagePart("1к12@dmg.acid")));
        effect.setAreaOfEffect(area);
        trait.setEffect(effect);
        creature.setTraits(List.of(trait));

        Map<?, ?> mapped = (Map<?, ?>) ((List<?>) mapper.toVttg(creature).getSystem().get("traits")).getFirst();
        Map<?, ?> mappedArea = (Map<?, ?>) mapped.get("areaOfEffect");

        assertEquals("constitution", mapped.get("saveType"));
        assertEquals(14, mapped.get("saveDC"));
        assertEquals("none", mapped.get("saveEffect"));
        // Эманация уезжает кругом: шаблона `emanation` в словаре VTTG нет.
        assertEquals("circle", mappedArea.get("shape"));
        assertEquals(5, mappedArea.get("size"));
        assertDamagePart(mapped, "1к12@dmg.acid", null);
    }

    /**
     * Поля старого импорта лежат рядом с записью, а не внутри механики: у записи, которую ни
     * разу не открывали в мастерской, механики нет вовсе. Заполнены они честно, поэтому едут
     * в выгрузку — в отличие от урона, который так и остался текстом описания.
     */
    @Test
    void exportsLegacyAttackTypeAndSaveWithoutMechanics() {
        Creature creature = creature("dragon-mm");
        CreatureAction action = action(
                "Ледяное дыхание",
                "[\"*Попадание:* 18 (4к8) урона холодом.\"]"
        );
        action.setEnglish("Cold Breath");
        action.setAttackType(AttackType.RANGE);
        action.setSawingThrows(List.of(savingThrow(Ability.CONSTITUTION, 15)));
        creature.setActions(List.of(action));

        Map<?, ?> mapped = firstAction(mapper.toVttg(creature).getSystem());

        assertEquals("Cold Breath", mapped.get("nameEn"));
        assertEquals("constitution", mapped.get("saveType"));
        assertEquals(15, mapped.get("saveDC"));
        assertEquals("ranged", mapped.get("rangeType"));
        assertNull(mapped.get("damageParts"));
    }

    /** Спасбросок заменяет бросок попадания — как в форме системы. */
    @Test
    void authoredSaveReplacesAttackBonus() {
        Creature creature = creature("dragon-mm");
        CreatureAction action = new CreatureAction();
        action.setName("Огненное дыхание");
        action.setDescription("[\"Существо выдыхает огонь.\"]");

        CreatureActionEffect effect = new CreatureActionEffect();
        effect.setAttackBonus(7);
        effect.setSavingThrows(List.of(savingThrow(Ability.DEXTERITY, 17)));
        effect.setSaveEffect(SpellSaveEffect.HALF);
        effect.setDamageParts(List.of(damagePart("8к6@dmg.fire")));

        AreaOfEffect area = new AreaOfEffect();
        area.setType(AreaOfEffectType.CONE);
        area.setValue1(30);
        effect.setAreaOfEffect(area);
        action.setEffect(effect);
        creature.setActions(List.of(action));

        Map<?, ?> mapped = firstAction(mapper.toVttg(creature).getSystem());
        Map<?, ?> mappedArea = (Map<?, ?>) mapped.get("areaOfEffect");

        assertNull(mapped.get("attackBonus"));
        assertEquals("dexterity", mapped.get("saveType"));
        assertEquals(17, mapped.get("saveDC"));
        assertEquals("half", mapped.get("saveEffect"));
        assertEquals("cone", mappedArea.get("shape"));
        assertEquals(30, mappedArea.get("size"));
    }

    /**
     * Пустой объект области форма мастерской шлёт у КАЖДОЙ записи, а размер на ней
     * необязателен: шаблон нулевого размера на столе бесполезен.
     */
    @Test
    void skipsAreaWithoutShapeOrSize() {
        assertNull(mappedArea(new AreaOfEffect()));

        AreaOfEffect shapeOnly = new AreaOfEffect();
        shapeOnly.setType(AreaOfEffectType.CONE);
        assertNull(mappedArea(shapeOnly));
    }

    /** Словарь форм у существа и у заклинания один: сфера и линия — круг и луч. */
    @Test
    void translatesAuthoredAreaShapesToVttgTemplates() {
        assertEquals("circle", authoredShape(AreaOfEffectType.SPHERE));
        assertEquals("circle", authoredShape(AreaOfEffectType.EMANATION));
        assertEquals("circle", authoredShape(AreaOfEffectType.CYLINDER));
        assertEquals("ray", authoredShape(AreaOfEffectType.LINE));
        assertEquals("rect", authoredShape(AreaOfEffectType.CUBE));
    }

    /**
     * Инициатива уезжает тем же расчётом, что и на карточке сайта: модификатор ЛОВ плюс бонус
     * мастерства за множитель. Пара «13 / +3» — это пассивное значение и сам модификатор.
     */
    @Test
    void exportsInitiative() {
        Creature creature = creature("goblin-mm");
        creature.setAbilities(abilitiesWithDexterity(16));

        Map<String, Object> system = mapper.toVttg(creature).getSystem();
        Map<?, ?> initiative = (Map<?, ?>) system.get("initiative");

        assertEquals("13", initiative.get("label"));
        assertEquals("+3", initiative.get("value"));
        assertEquals(3, system.get("initiativeBonus"));
    }

    /** Владение инициативой из статблока добавляет бонус мастерства по опасности. */
    @Test
    void addsProficiencyBonusToInitiativeWhenCreatureIsProficient() {
        Creature creature = creature("goblin-mm");
        creature.setAbilities(abilitiesWithDexterity(16));
        creature.setExperience(450L);
        CreatureInitiative initiative = new CreatureInitiative();
        initiative.setMultiplier((byte) 1);
        creature.setInitiative(initiative);

        Map<String, Object> system = mapper.toVttg(creature).getSystem();

        assertEquals(5, system.get("initiativeBonus"));
        assertEquals("15", ((Map<?, ?>) system.get("initiative")).get("label"));
    }

    /** Секция описания: подзаголовок, места обитания, сокровища и текст — всё в одном блоке. */
    @Test
    void exportsDescriptionSection() {
        Creature creature = creature("aboleth-mm");
        CreatureSection section = new CreatureSection();
        section.setSectionName("Аболеты");
        section.setSubtitle("Владыки глубин");
        section.setSectionDescription("[\"Текст секции\"]");
        section.setHabitats(List.of(Habitat.UNDERWATER, Habitat.UNDERDARK));
        section.setTreasures(List.of(CreatureTreasure.ARCANA, CreatureTreasure.RELICS));
        creature.setSection(section);

        Map<?, ?> mapped = (Map<?, ?>) mapper.toVttg(creature).getSystem().get("section");

        assertEquals("Аболеты", mapped.get("name"));
        assertEquals("Владыки глубин", mapped.get("subtitle"));
        assertEquals("Текст секции", mapped.get("description"));
        assertEquals("Магия, Реликвии", mapped.get("treasures"));
    }

    /** Секции нет — ключа тоже нет: пустой блок на листе не нужен. */
    @Test
    void skipsSectionWhenEmpty() {
        Map<String, Object> system = mapper.toVttg(creature("goblin-mm")).getSystem();

        assertFalse(system.containsKey("section"));
    }

    /**
     * Словарь сред у VTTG короче нашего: все планы схлопываются в один ключ «planar». Название
     * конкретного плана сохраняется в особых средах, иначе оно терялось бы совсем.
     */
    @Test
    void keepsPlaneNamesInCustomEnvironments() {
        Creature creature = creature("demon-mm");
        CreatureSection section = new CreatureSection();
        section.setHabitats(List.of(Habitat.FOREST, Habitat.PLANAR_ABYSS, Habitat.PLANAR_FEYWILD));
        creature.setSection(section);

        Map<String, Object> system = mapper.toVttg(creature).getSystem();

        assertEquals(List.of("forest", "planar"), system.get("environments"));
        assertEquals("План (Бездна); План (Страна фей)", system.get("customEnvironments"));
    }

    /** Перезарядка едет ключом словаря: подпись «Перезарядка 5–6» рисует уже система. */
    @Test
    void exportsRecharge() {
        Creature creature = creature("dragon-mm");
        CreatureAction action = action("Огненное дыхание", "[\"Существо выдыхает огонь.\"]");
        action.setRecharge(RechargeType.D5);
        creature.setActions(List.of(action));

        CreatureTrait trait = new CreatureTrait();
        trait.setName("Легендарное сопротивление");
        trait.setDescription("[\"Существо преуспевает в спасброске.\"]");
        trait.setRecharge(RechargeType.LR);
        creature.setTraits(List.of(trait));

        Map<String, Object> system = mapper.toVttg(creature).getSystem();
        Map<?, ?> mappedTrait = (Map<?, ?>) ((List<?>) system.get("traits")).getFirst();

        assertEquals("d5", firstAction(system).get("recharge"));
        assertEquals("lr", mappedTrait.get("recharge"));
    }

    /** Перезарядки нет — ключа нет. */
    @Test
    void skipsRechargeWhenNotSet() {
        Creature creature = creature("goblin-mm");
        creature.setActions(List.of(action("Укус", "[\"Существо кусает.\"]")));

        assertNull(firstAction(mapper.toVttg(creature).getSystem()).get("recharge"));
    }

    /**
     * Оговорки защит («колющий от немагических атак») в ключи не укладываются: на листе они
     * идут отдельной строкой под значками, поэтому едут своими текстовыми полями.
     */
    @Test
    void exportsDefenseTexts() {
        Creature creature = creature("werewolf-mm");
        creature.setVulnerabilities(List.of(DamageType.FIRE));
        creature.setResistance(List.of(DamageType.PIERCING));
        creature.setResistanceText("колющий от немагических атак");
        creature.setImmunityToCondition(List.of(Condition.CHARMED));
        creature.setImmunityText("яд, пока бодрствует");
        creature.setVulnerabilitiesText("огонь при свете дня");

        Map<?, ?> defenses = (Map<?, ?>) mapper.toVttg(creature).getSystem().get("defenses");

        assertEquals(List.of("fire"), defenses.get("vulnerabilities"));
        assertEquals("огонь при свете дня", defenses.get("vulnerabilitiesText"));
        assertEquals("колющий от немагических атак", defenses.get("resistancesText"));
        assertEquals("яд, пока бодрствует", defenses.get("immunitiesText"));
    }

    /** Текстов защит нет — ключей нет: пустая строка на листе нарисовала бы пустую строку. */
    @Test
    void skipsDefenseTextsWhenEmpty() {
        Map<?, ?> defenses = (Map<?, ?>) mapper.toVttg(creature("goblin-mm")).getSystem().get("defenses");

        assertFalse(defenses.containsKey("vulnerabilitiesText"));
        assertFalse(defenses.containsKey("resistancesText"));
        assertFalse(defenses.containsKey("immunitiesText"));
    }

    /**
     * Строка инвентаря — для чтения: количество в ней стоит словом, и в позицию такое
     * не укладывается.
     */
    @Test
    void exportsInventoryText() {
        Creature creature = creature("goblin-mm");
        creature.setInventoryText("три кинжала и кожаный доспех");

        assertEquals("три кинжала и кожаный доспех", mapper.toVttg(creature).getSystem().get("gear"));
    }

    /**
     * Своей строки нет — едет снаряжение старого импорта: у существ, которым инвентарь
     * ещё не завели, весь текст лежит только там.
     */
    @Test
    void fallsBackToLegacyEquipmentsForGearLine() {
        Creature creature = creature("goblin-mm");
        creature.setEquipments("Кинжал, кожаный доспех");

        assertEquals("Кинжал, кожаный доспех", mapper.toVttg(creature).getSystem().get("gear"));
    }

    /**
     * Позиции инвентаря заводят руками карточками сайта. По ним VTTG кладёт предмет в
     * сумку существа со своим весом и боевыми полями, а не заводит его по названию.
     */
    @Test
    @SuppressWarnings("unchecked")
    void exportsInventoryItems() {
        Creature creature = creature("goblin-mm");
        creature.setInventory(List.of(
                inventoryItem("dagger-phb", "Кинжал", 3),
                inventoryItem("shield-phb", "Щит", null)
        ));

        Map<String, Object> system = mapper.toVttg(creature).getSystem();
        List<VttgEquipmentItem> items = (List<VttgEquipmentItem>) system.get("gearItems");

        assertEquals(2, items.size());
        assertEquals("dagger-phb", items.get(0).url());
        assertEquals("Кинжал", items.get(0).name());
        assertEquals(3, items.get(0).quantity());
        assertEquals("shield-phb", items.get(1).url());
        // Одиночная позиция количества не несёт: в компендиуме единица подразумевается.
        assertNull(items.get(1).quantity());
    }

    /**
     * Ссылки в строке снаряжения позициями не становятся: инвентарь заводят руками, а
     * разбор текста давал предмет без веса, стоимости и боевых полей.
     */
    @Test
    void doesNotDeriveInventoryFromText() {
        Creature creature = creature("goblin-mm");
        creature.setEquipments("три {@item Кинжала|url:dagger-phb}");

        assertNull(mapper.toVttg(creature).getSystem().get("gearItems"));
    }

    /** Описание блока легендарных действий уезжает рядом с их числом. */
    @Test
    void exportsLegendaryDescription() {
        Creature creature = creature("dragon-mm");
        creature.setLegendaryAction((byte) 3);
        creature.setLegendaryDescription("Существо совершает три легендарных действия.");

        Map<?, ?> legendary = (Map<?, ?>) mapper.toVttg(creature).getSystem().get("legendary");

        assertEquals((byte) 3, legendary.get("count"));
        assertEquals("Существо совершает три легендарных действия.", legendary.get("description"));
    }

    /** Опыт и число легендарных действий в логове лежат на существе, а уезжают в блок логова. */
    @Test
    void exportsLairExperienceAndLegendaryCount() {
        Creature creature = creature("dragon-mm");
        creature.setExperienceInLair(18000L);
        creature.setLegendaryActionInLair((byte) 4);
        CreatureLair lair = new CreatureLair();
        lair.setName("Логово дракона");
        creature.setLair(lair);

        Map<?, ?> mapped = (Map<?, ?>) mapper.toVttg(creature).getSystem().get("lair");

        assertEquals(18000L, mapped.get("experience"));
        assertEquals(4, mapped.get("legendaryActionCount"));
    }

    /** Нулевые поправки логова не кладём: ноль легендарных действий — это их отсутствие. */
    @Test
    void skipsEmptyLairCorrections() {
        Creature creature = creature("goblin-mm");
        CreatureLair lair = new CreatureLair();
        lair.setName("Логово");
        creature.setLair(lair);

        Map<?, ?> mapped = (Map<?, ?>) mapper.toVttg(creature).getSystem().get("lair");

        assertFalse(mapped.containsKey("experience"));
        assertFalse(mapped.containsKey("legendaryActionCount"));
    }

    /**
     * Порции блока задают заряды заклинания: «по желанию» — неограниченный счётчик,
     * «1/день каждое» — свой заряд каждому заклинанию порции, а общий пул порции
     * заклинанию невыразим, и зарядов у него нет вовсе.
     */
    @Test
    void exportsCreatureSpellsWithGroupUses() {
        catalog("dancing-lights-phb", "hold-person-phb", "augury-phb");
        Creature creature = creature("green-hag-mm");
        CreatureSpellcastingBlock block = block("spellcasting-1", "Использование заклинаний");
        block.setAbility(Ability.WISDOM);
        block.setGroups(List.of(
                group(CreatureSpellUsageMode.AT_WILL, spellRef("dancing-lights-phb")),
                counted(CreatureSpellUsageMode.PER_DAY_EACH, 1, spellRef("hold-person-phb")),
                counted(CreatureSpellUsageMode.PER_DAY_POOL, 1, spellRef("augury-phb"))));
        creature.setSpellcasting(List.of(block));

        List<VttgSpell> spells = mapper.toVttg(creature).getSpells();

        assertEquals(List.of("dancing-lights-phb", "hold-person-phb", "augury-phb"),
                spells.stream().map(VttgSpell::getId).toList());
        assertUses(spells.getFirst().getUses(), 0, "atWill");
        assertUses(spells.get(1).getUses(), 1, "longRest");
        assertNull(spells.getLast().getUses());
        assertTrue(spells.stream().allMatch(spell -> "wisdom".equals(spell.getSpellcastingAbility())));
    }

    /** «N за короткий отдых каждое» отдаёт заряды, восстанавливаемые коротким отдыхом. */
    @Test
    void exportsShortRestUsesForRestGroup() {
        catalog("shield-phb");
        Creature creature = creature("cambion-mm");
        CreatureSpellcastingBlock block = block("spellcasting-1", "Использование заклинаний");
        CreatureSpellGroup group = counted(CreatureSpellUsageMode.PER_REST_EACH, 2, spellRef("shield-phb"));
        group.setRest(CreatureSpellRestKind.SHORT);
        block.setGroups(List.of(group));
        creature.setSpellcasting(List.of(block));

        assertUses(mapper.toVttg(creature).getSpells().getFirst().getUses(), 2, "shortRest");
    }

    /**
     * Одно и то же заклинание в двух порциях уезжает одной записью: на листе существа
     * заклинание одно, и дубль показал бы у него два счётчика.
     */
    @Test
    void exportsRepeatedSpellOnceByFirstGroup() {
        catalog("invisibility-phb");
        Creature creature = creature("green-hag-mm");
        CreatureSpellcastingBlock block = block("spellcasting-1", "Использование заклинаний");
        block.setGroups(List.of(
                group(CreatureSpellUsageMode.AT_WILL, spellRef("invisibility-phb")),
                counted(CreatureSpellUsageMode.PER_DAY_EACH, 3, spellRef("invisibility-phb"))));
        creature.setSpellcasting(List.of(block));

        List<VttgSpell> spells = mapper.toVttg(creature).getSpells();

        assertEquals(1, spells.size());
        assertUses(spells.getFirst().getUses(), 0, "atWill");
    }

    /**
     * Раскладка уезжает в {@code system.spellcastingBlocks} — тем же ключом, каким её
     * пишет мир, — а запасные числа существа в {@code system.spellcasting}. Запасные
     * берутся у первого блока, где они заданы: блоков бывает несколько с разными
     * характеристикой и Сл, а запасное число на существо одно.
     */
    @Test
    void exportsSpellcastingBlocksIntoSystem() {
        catalog("ray-of-sickness-phb", "hold-person-phb");
        Creature creature = creature("green-hag-mm");

        CreatureSpellcastingBlock coven = block("spellcasting-coven", "Магия шабаша");
        coven.setNote("находясь в пределах 30 футов от двух союзных карг");
        coven.setAbility(Ability.INTELLIGENCE);
        coven.setSaveDc(11);
        CreatureSpellRef ray = spellRef("ray-of-sickness-phb");
        ray.setCastLevel(3);
        ray.setNote("только на себя");
        coven.setGroups(List.of(group(CreatureSpellUsageMode.AT_WILL, ray)));

        CreatureSpellcastingBlock own = block("spellcasting-own", "Использование заклинаний");
        own.setAbility(Ability.WISDOM);
        own.setSaveDc(12);
        own.setAttackBonus(4);
        own.setIgnoredComponents(ignoredMaterial());
        CreatureSpellGroup pool = counted(CreatureSpellUsageMode.PER_REST_POOL, 1, spellRef("hold-person-phb"));
        pool.setRest(CreatureSpellRestKind.LONG);
        pool.setRecharge(RechargeType.D5);
        pool.setLabel("1/день");
        own.setGroups(List.of(pool));

        creature.setSpellcasting(List.of(coven, own));

        Map<String, Object> system = mapper.toVttg(creature).getSystem();
        Map<?, ?> spellcasting = (Map<?, ?>) system.get("spellcasting");

        assertEquals("intelligence", spellcasting.get("ability"));
        assertEquals(11, spellcasting.get("saveDC"));
        assertEquals(4, spellcasting.get("attackBonus"));

        List<?> blocks = (List<?>) system.get("spellcastingBlocks");
        Map<?, ?> mappedCoven = (Map<?, ?>) blocks.getFirst();
        assertEquals("spellcasting-coven", mappedCoven.get("id"));
        assertEquals("Магия шабаша", mappedCoven.get("name"));
        assertEquals("находясь в пределах 30 футов от двух союзных карг", mappedCoven.get("note"));
        assertEquals("intelligence", mappedCoven.get("ability"));
        assertFalse(mappedCoven.containsKey("attackBonus"));
        Map<?, ?> covenGroup = (Map<?, ?>) ((List<?>) mappedCoven.get("groups")).getFirst();
        assertEquals("atWill", covenGroup.get("mode"));
        assertEquals(List.of(Map.of("id", "ray-of-sickness-phb", "castLevel", 3, "note", "только на себя")),
                covenGroup.get("spells"));

        Map<?, ?> mappedOwn = (Map<?, ?>) blocks.getLast();
        assertEquals(Map.of("verbal", false, "somatic", false, "material", true),
                mappedOwn.get("ignoredComponents"));
        assertFalse(mappedCoven.containsKey("ignoredComponents"));
        Map<?, ?> ownGroup = (Map<?, ?>) ((List<?>) mappedOwn.get("groups")).getFirst();
        assertEquals("perRestPool", ownGroup.get("mode"));
        assertEquals(1, ownGroup.get("count"));
        assertEquals("long", ownGroup.get("rest"));
        assertEquals("d5", ownGroup.get("recharge"));
        assertEquals("1/день", ownGroup.get("label"));
    }

    /** Существо без блоков выгружается как раньше: ни заклинаний, ни ключа заклинательства. */
    @Test
    void skipsSpellcastingWithoutBlocks() {
        Creature creature = creature("goblin-mm");

        VttgCreature mapped = mapper.toVttg(creature);

        assertNull(mapped.getSpells());
        assertFalse(mapped.getSystem().containsKey("spellcasting"));
        assertFalse(mapped.getSystem().containsKey("spellcastingBlocks"));
    }

    /**
     * Недозаполненный блок выгрузку не роняет: автор оставляет блоки и порции пустыми
     * между заходами, и отбрасывать их на бэке нельзя.
     */
    @Test
    void exportsBlockWithoutFilledFields() {
        Creature creature = creature("goblin-mm");
        CreatureSpellcastingBlock block = new CreatureSpellcastingBlock();
        block.setGroups(List.of(new CreatureSpellGroup()));
        creature.setSpellcasting(List.of(block));

        VttgCreature mapped = mapper.toVttg(creature);
        List<?> blocks = (List<?>) mapped.getSystem().get("spellcastingBlocks");
        Map<?, ?> mappedBlock = (Map<?, ?>) blocks.getFirst();

        assertNull(mapped.getSpells());
        // Запасных чисел у блока нет — и ключа нет: пустая настройка выглядела бы заданной
        assertFalse(mapped.getSystem().containsKey("spellcasting"));
        assertEquals(List.of(), ((Map<?, ?>) ((List<?>) mappedBlock.get("groups")).getFirst()).get("spells"));
    }

    /** Заклинание, чью карточку удалили, в список не попадает — собирать запись не из чего. */
    @Test
    void skipsSpellMissingFromCatalog() {
        catalog("hold-person-phb");
        Creature creature = creature("green-hag-mm");
        CreatureSpellcastingBlock block = block("spellcasting-1", "Использование заклинаний");
        block.setGroups(List.of(group(CreatureSpellUsageMode.AT_WILL,
                spellRef("hold-person-phb"), spellRef("removed-spell"))));
        creature.setSpellcasting(List.of(block));

        VttgCreature mapped = mapper.toVttg(creature);

        assertEquals(List.of("hold-person-phb"),
                mapped.getSpells().stream().map(VttgSpell::getId).toList());
        List<?> blocks = (List<?>) mapped.getSystem().get("spellcastingBlocks");
        Map<?, ?> mappedBlock = (Map<?, ?>) blocks.getFirst();
        Map<?, ?> mappedGroup = (Map<?, ?>) ((List<?>) mappedBlock.get("groups")).getFirst();
        assertEquals(List.of(Map.of("id", "hold-person-phb"), Map.of("id", "removed-spell")),
                mappedGroup.get("spells"));
    }

    private void catalog(String... urls) {
        when(spellRepository.findAllForVttgExportByUrls(any()))
                .thenReturn(Arrays.stream(urls).map(this::spell).toList());
    }

    private Spell spell(String url) {
        Spell result = new Spell();
        result.setUrl(url);
        result.setName(url);
        result.setLevel(2L);
        result.setSchool(SpellSchool.builder().school(MagicSchool.ENCHANTMENT).build());
        result.setDescription("[\"Описание заклинания\"]");
        return result;
    }

    private CreatureSpellcastingBlock block(String id, String name) {
        CreatureSpellcastingBlock result = new CreatureSpellcastingBlock();
        result.setId(id);
        result.setName(name);
        return result;
    }

    private CreatureSpellGroup group(CreatureSpellUsageMode mode, CreatureSpellRef... spells) {
        CreatureSpellGroup result = new CreatureSpellGroup();
        result.setMode(mode);
        result.setSpells(List.of(spells));
        return result;
    }

    private CreatureSpellGroup counted(CreatureSpellUsageMode mode, int count, CreatureSpellRef... spells) {
        CreatureSpellGroup result = group(mode, spells);
        result.setCount(count);
        return result;
    }

    private CreatureSpellRef spellRef(String url) {
        CreatureSpellRef result = new CreatureSpellRef();
        result.setUrl(url);
        result.setName(url);
        return result;
    }

    private CreatureSpellComponents ignoredMaterial() {
        CreatureSpellComponents result = new CreatureSpellComponents();
        result.setMaterial(true);
        return result;
    }

    private void assertUses(VttgSpellUses uses, int expectedMax, String expectedRecovery) {
        assertEquals(expectedMax, uses.getMax());
        assertEquals(expectedMax, uses.getCurrent());
        assertEquals(expectedRecovery, uses.getRecovery());
    }

    private String authoredShape(AreaOfEffectType type) {
        AreaOfEffect area = new AreaOfEffect();
        area.setType(type);
        area.setValue1(20);

        return (String) mappedArea(area).get("shape");
    }

    private Map<?, ?> mappedArea(AreaOfEffect area) {
        Creature creature = creature("dragon-mm");
        CreatureAction action = new CreatureAction();
        action.setName("Дыхание");
        action.setDescription("[\"Существо выдыхает.\"]");

        CreatureActionEffect effect = new CreatureActionEffect();
        effect.setAreaOfEffect(area);
        action.setEffect(effect);
        creature.setActions(List.of(action));

        return (Map<?, ?>) firstAction(mapper.toVttg(creature).getSystem()).get("areaOfEffect");
    }

    private EquipmentItem inventoryItem(String url, String name, Integer quantity) {
        EquipmentItem result = new EquipmentItem();
        result.setUrl(url);
        result.setName(name);
        result.setQuantity(quantity);
        return result;
    }

    private DamagePart damagePart(String formula) {
        DamagePart result = new DamagePart();
        result.setFormula(formula);
        return result;
    }

    private ActiveEffect activeEffect(String id) {
        ActiveEffect result = new ActiveEffect();
        result.setId(id);
        return result;
    }

    private SawingThrow savingThrow(Ability ability, int dc) {
        SawingThrow result = new SawingThrow();
        result.setAbility(ability);
        result.setDc((byte) dc);
        return result;
    }

    private Creature creature(String url) {
        Creature result = new Creature();
        result.setUrl(url);
        result.setName(url);
        result.setDescription("");
        return result;
    }

    private CreatureAbilities abilitiesWithDexterity(int value) {
        CreatureAbilities result = new CreatureAbilities();
        result.setDexterity(ability(Ability.DEXTERITY, value, 0));
        return result;
    }

    private CreatureAbility ability(Ability ability, int value, int multiplier) {
        CreatureAbility result = new CreatureAbility();
        result.setAbility(ability);
        result.setValue((short) value);
        result.setMultiplier((byte) multiplier);
        return result;
    }

    private CreatureAction action(String name, String description) {
        CreatureAction result = new CreatureAction();
        result.setName(name);
        result.setDescription(description);
        return result;
    }

    private void assertActionDescription(Map<?, ?> system, String key, List<String> expectedDescription) {
        List<?> actions = (List<?>) system.get(key);
        Map<?, ?> action = (Map<?, ?>) actions.getFirst();
        assertEquals(expectedDescription, action.get("description"));
    }

    private void assertLegendaryActionDescription(Map<?, ?> system, List<String> expectedDescription) {
        Map<?, ?> legendary = (Map<?, ?>) system.get("legendary");
        assertActionDescription(legendary, "actions", expectedDescription);
    }

    private Map<?, ?> firstAction(Map<?, ?> system) {
        List<?> actions = (List<?>) system.get("actions");
        return (Map<?, ?>) actions.getFirst();
    }

    private void assertActiveEffect(Map<?, ?> action, String expectedId) {
        List<?> activeEffects = (List<?>) action.get("activeEffects");
        assertTrue(activeEffects.stream()
                .map(ActiveEffect.class::cast)
                .anyMatch(effect -> expectedId.equals(effect.getId())));
    }

    private void assertDamagePart(Map<?, ?> action, String expectedFormula, String expectedType) {
        List<?> damageParts = (List<?>) action.get("damageParts");
        Map<?, ?> damagePart = (Map<?, ?>) damageParts.getFirst();
        assertEquals(expectedFormula, damagePart.get("formula"));
        assertEquals(expectedType, damagePart.get("type"));
    }

    private void assertToken(Map<?, ?> token) {
        assertEquals("assets/token-frames/0.png", token.get("frameUrl"));
        assertEquals("https://new.ttg.club/s3/bestiary/magistrus/1757076204886-badger.webp", token.get("imageUrl"));
        assertEquals(true, token.get("showName"));
        assertEquals("hostile", token.get("disposition"));
        assertEquals(2, token.get("scale"));

        Map<?, ?> vision = (Map<?, ?>) token.get("vision");
        assertEquals(60, vision.get("range"));
        assertEquals(60, vision.get("darkvision"));
        assertEquals(360, vision.get("angle"));
        assertEquals(true, vision.get("enabled"));
    }
}

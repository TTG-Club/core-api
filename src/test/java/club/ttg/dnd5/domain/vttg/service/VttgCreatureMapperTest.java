package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.model.CreatureAbilities;
import club.ttg.dnd5.domain.beastiary.model.CreatureAbility;
import club.ttg.dnd5.domain.beastiary.model.CreatureArmor;
import club.ttg.dnd5.domain.beastiary.model.CreatureCategory;
import club.ttg.dnd5.domain.beastiary.model.CreatureHit;
import club.ttg.dnd5.domain.beastiary.model.CreatureLair;
import club.ttg.dnd5.domain.beastiary.model.CreatureSection;
import club.ttg.dnd5.domain.beastiary.model.CreatureSize;
import club.ttg.dnd5.domain.beastiary.model.CreatureTrait;
import club.ttg.dnd5.domain.beastiary.model.action.CreatureAction;
import club.ttg.dnd5.domain.beastiary.model.action.SawingThrow;
import club.ttg.dnd5.domain.beastiary.model.sense.Senses;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Alignment;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Habitat;
import club.ttg.dnd5.domain.common.dictionary.Size;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VttgCreatureMapperTest {
    private final VttgCreatureMapper mapper = new VttgCreatureMapper(new VttgMarkupConverter(new ObjectMapper()));

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
    void extractsMeleeActionMechanicsFromDescription() {
        Creature creature = new Creature();
        creature.setUrl("melee-creature");
        creature.setName("Melee Creature");
        creature.setDescription("");
        creature.setActions(List.of(action(
                "Bite",
                "[\"Melee Weapon Attack: +5 to hit, reach 10 ft., one target. Hit: 7 (1d8 + 3) piercing damage.\"]"
        )));

        Map<?, ?> action = firstAction(mapper.toVttg(creature).getSystem());

        assertEquals(5, action.get("attackBonus"));
        assertDamagePart(action, "1к8 + 3", "piercing");
        assertFalse(action.containsKey("damageDice"));
        assertFalse(action.containsKey("damageType"));
        assertEquals(10, action.get("reach"));
        assertEquals("melee", action.get("rangeType"));
        assertEquals("ft", action.get("distanceUnit"));
    }

    @Test
    void extractsRangedActionMechanicsFromDescription() {
        Creature creature = new Creature();
        creature.setUrl("ranged-creature");
        creature.setName("Ranged Creature");
        creature.setDescription("");
        creature.setActions(List.of(action(
                "Web",
                "[\"Ranged Weapon Attack: +5 to hit, range 30/60 ft., one target. Hit: the target is restrained.\"]"
        )));

        Map<?, ?> action = firstAction(mapper.toVttg(creature).getSystem());
        Map<?, ?> range = (Map<?, ?>) action.get("range");

        assertEquals(5, action.get("attackBonus"));
        assertEquals("ranged", action.get("rangeType"));
        assertEquals("ft", action.get("distanceUnit"));
        assertEquals(30, range.get("normal"));
        assertEquals(60, range.get("long"));
        assertActiveEffect(action, "restrained");
    }

    @Test
    void extractsFlatDamageFromDescription() {
        Creature creature = new Creature();
        creature.setUrl("flat-damage-creature");
        creature.setName("Flat Damage Creature");
        creature.setDescription("");
        creature.setActions(List.of(action(
                "Bite",
                "[\"Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 1 piercing damage, and the target must save.\"]"
        )));

        Map<?, ?> action = firstAction(mapper.toVttg(creature).getSystem());

        assertDamagePart(action, "1", "piercing");
    }

    @Test
    void doesNotExtractOngoingDamageAsAttackDamage() {
        Creature creature = new Creature();
        creature.setUrl("ongoing-damage-creature");
        creature.setName("Ongoing Damage Creature");
        creature.setDescription("");
        creature.setActions(List.of(action(
                "Swallow",
                "[\"The swallowed target takes 16d6 acid damage at the start of each of the creature's turns.\"]"
        )));

        Map<?, ?> action = firstAction(mapper.toVttg(creature).getSystem());

        assertNull(action.get("damageParts"));
    }

    @Test
    void extractsConditionEffectsFromDescription() {
        Creature creature = new Creature();
        creature.setUrl("condition-creature");
        creature.setName("Condition Creature");
        creature.setDescription("");
        creature.setActions(List.of(action(
                "Slam",
                "[\"Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 10 (2d6 + 3) bludgeoning damage, "
                        + "and the target is knocked prone.\"]"
        )));

        Map<?, ?> action = firstAction(mapper.toVttg(creature).getSystem());

        assertActiveEffect(action, "prone");
    }

    @Test
    void exportsEnglishNameStructuredDamageTypeAndSavingThrow() {
        Creature creature = new Creature();
        creature.setUrl("save-creature");
        creature.setName("Save Creature");
        creature.setDescription("");

        CreatureAction action = action(
                "Ледяное дыхание",
                "[\"Each creature must make a DC 15 Constitution saving throw, taking 18 (4d8) damage, or half damage on success.\"]"
        );
        action.setEnglish("Cold Breath");
        action.setDamageTypes(List.of(DamageType.COLD));
        SawingThrow savingThrow = new SawingThrow();
        savingThrow.setAbility(Ability.CONSTITUTION);
        savingThrow.setDc((byte) 15);
        action.setSawingThrows(List.of(savingThrow));
        creature.setActions(List.of(action));

        Map<?, ?> mappedAction = firstAction(mapper.toVttg(creature).getSystem());

        assertEquals("Cold Breath", mappedAction.get("nameEn"));
        assertDamagePart(mappedAction, "4к8", "cold");
        assertEquals("constitution", mappedAction.get("saveType"));
        assertEquals(15, mappedAction.get("saveDC"));
        assertEquals("half", mappedAction.get("saveEffect"));
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
                .map(Map.class::cast)
                .anyMatch(effect -> expectedId.equals(effect.get("id"))));
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

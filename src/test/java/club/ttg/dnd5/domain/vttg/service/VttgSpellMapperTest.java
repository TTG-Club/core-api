package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.beastiary.model.action.AttackType;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.spell.model.AreaOfEffect;
import club.ttg.dnd5.domain.spell.model.MaterialComponent;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.model.SpellCastingTime;
import club.ttg.dnd5.domain.spell.model.SpellComponents;
import club.ttg.dnd5.domain.spell.model.SpellDistance;
import club.ttg.dnd5.domain.spell.model.SpellDuration;
import club.ttg.dnd5.domain.spell.model.SpellEffect;
import club.ttg.dnd5.domain.spell.model.SpellSchool;
import club.ttg.dnd5.domain.spell.model.enums.AreaOfEffectType;
import club.ttg.dnd5.domain.spell.model.enums.CastingUnit;
import club.ttg.dnd5.domain.spell.model.enums.DistanceUnit;
import club.ttg.dnd5.domain.spell.model.enums.DurationUnit;
import club.ttg.dnd5.domain.spell.model.enums.MagicSchool;
import club.ttg.dnd5.domain.spell.model.enums.SpellTargetType;
import club.ttg.dnd5.domain.spell.model.enums.SpellSaveEffect;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VttgSpellMapperTest {
    private final VttgMarkupConverter markupConverter = new VttgMarkupConverter(new ObjectMapper());
    private final VttgSpellMapper mapper = new VttgSpellMapper(
            markupConverter,
            new VttgSpellMechanicsExtractor(),
            new VttgSpellScalingExtractor()
    );
    private final VttgClassMapper classMapper = new VttgClassMapper(markupConverter);

    @Test
    void mapsStructuredSpellFieldsToVttgFormat() {
        Spell spell = new Spell();
        spell.setUrl("fire-burst");
        spell.setName("Огненная вспышка");
        spell.setEnglish("Fire Burst");
        spell.setLevel(3L);
        spell.setSchool(SpellSchool.builder().school(MagicSchool.EVOCATION).build());
        spell.setCastingTime(List.of(
                SpellCastingTime.of(1L, CastingUnit.ACTION),
                SpellCastingTime.of(null, CastingUnit.RITUAL)
        ));
        spell.setComponents(SpellComponents.builder()
                .v(true)
                .s(true)
                .m(MaterialComponent.builder()
                        .text("щепотка серы")
                        .consumable(false)
                        .build())
                .build());
        spell.setRange(List.of(SpellDistance.of(120L, DistanceUnit.FEET)));
        spell.setDuration(List.of(SpellDuration.builder()
                .value(1L)
                .unit(DurationUnit.MINUTE)
                .concentration(true)
                .build()));
        AreaOfEffect area = new AreaOfEffect();
        area.setType(AreaOfEffectType.SPHERE);
        area.setValue1(20);
        SpellEffect effect = new SpellEffect();
        effect.setAreaOfEffect(area);
        effect.setAttackType(AttackType.RANGE);
        effect.setAutoHit(true);
        effect.setTargetCount(3);
        effect.setTargetType(SpellTargetType.AREA);
        effect.setDamageFormulas(List.of("8к6@dmg.fire"));
        effect.setSavingThrows(List.of(Ability.DEXTERITY));
        effect.setSaveEffect(SpellSaveEffect.HALF);
        spell.setEffect(effect);
        spell.setDescription("[\"Первый абзац\",\"Второй абзац\"]");
        spell.setUpper("[\"Урон увеличивается на {@roll 1к6} за каждый уровень ячейки выше 3.\"]");
        spell.setSrdVersion("2.5");
        Source source = new Source();
        source.setAcronym("PHB24");
        spell.setSource(source);
        spell.setSourcePage(241L);

        var result = mapper.toVttg(spell);

        assertEquals("fire-burst", result.getId());
        assertEquals("evocation", result.getSchool());
        assertEquals("action", result.getCastingTimeUnit());
        assertTrue(result.isRitual());
        assertEquals("ft", result.getRangeUnit());
        assertEquals("minute", result.getDurationUnit());
        assertTrue(result.isConcentration());
        assertEquals("circle", result.getAreaOfEffect().getShape());
        assertEquals(20, result.getAreaOfEffect().getSize());
        assertEquals("area", result.getTargetType());
        assertEquals(3, result.getTargetCount());
        assertEquals("ranged", result.getDeliveryType());
        assertTrue(result.getAutoHit());
        assertEquals("8к6@dmg.fire", result.getDamageParts().getFirst().getFormula());
        assertEquals("dexterity", result.getSaveType());
        assertEquals("half", result.getSaveEffect());
        assertEquals("Первый абзац\n\nВторой абзац", result.getDescription());
        assertEquals("Урон увеличивается на 1к6 за каждый уровень ячейки выше 3.",
                result.getHigherLevelDescription());
        assertEquals("1к6", result.getScaling().getAdditionalDice());
        assertEquals(result.getHigherLevelDescription(), result.getScaling().getDescription());
        assertEquals("phb", result.getSourceKey());
        assertEquals("spell", result.getType());
    }

    @Test
    void mapsRealMarkupDescriptionWithoutLosingText() {
        Spell spell = new Spell();
        spell.setUrl("acid-splash");
        spell.setName("Брызги кислоты");
        spell.setEnglish("Acid Splash");
        spell.setLevel(0L);
        spell.setSchool(SpellSchool.builder().school(MagicSchool.CONJURATION).build());
        spell.setDescription("""
                ["{@i Вы бросаете кислотный шарик} в точку, где он взрывается {@glossary сферой|url:sphere-phb}. Цель получает {@roll 1к6} урона кислотой."]
                """);
        spell.setUpper("""
                ["The spell's damage increases by {@roll 1d6} when you reach levels 5 ({@roll 2d6}), 11 ({@roll 3d6}), and 17 ({@roll 4d6})."]
                """);

        var result = mapper.toVttg(spell);

        assertTrue(result.getDescription().startsWith("*Вы бросаете кислотный шарик*"));
        assertTrue(result.getDescription().contains("[сферой](https://ttg.club/glossary/sphere-phb)"));
        assertTrue(result.getDescription().contains("1к6"));
        assertEquals("1к6@dmg.acid", result.getDamageParts().getFirst().getFormula());
        assertEquals("level", result.getCantripScaling());
        assertEquals(5, result.getCantripScalingTiers().get(0).getLevel());
        assertEquals("2к6@dmg.acid", result.getCantripScalingTiers().get(0).getParts().getFirst().getFormula());
        assertEquals(11, result.getCantripScalingTiers().get(1).getLevel());
        assertEquals("3к6@dmg.acid", result.getCantripScalingTiers().get(1).getParts().getFirst().getFormula());
        assertEquals(17, result.getCantripScalingTiers().get(2).getLevel());
        assertEquals("4к6@dmg.acid", result.getCantripScalingTiers().get(2).getParts().getFirst().getFormula());
    }

    @Test
    void doesNotInferAttackDeliveryFromSpellRange() {
        Spell spell = new Spell();
        spell.setUrl("auto-hit-spell");
        spell.setName("Auto Hit Spell");
        spell.setEnglish("Auto Hit Spell");
        spell.setLevel(1L);
        spell.setSchool(SpellSchool.builder().school(MagicSchool.EVOCATION).build());
        spell.setRange(List.of(SpellDistance.of(120L, DistanceUnit.FEET)));

        SpellEffect effect = new SpellEffect();
        effect.setTargetType(SpellTargetType.CREATURE);
        effect.setAutoHit(true);
        effect.setDamageFormulas(List.of("3d4 + 3@dmg.force"));
        spell.setEffect(effect);

        var result = mapper.toVttg(spell);

        assertEquals("creature", result.getTargetType());
        assertEquals("none", result.getDeliveryType());
        assertEquals(120, result.getRange());
        assertEquals("ft", result.getRangeUnit());
        assertEquals("3d4@dmg.force+3", result.getDamageParts().getFirst().getFormula());
    }

    @Test
    void doesNotAddCantripScalingTiersWithoutCharacterLevelScalingText() {
        Spell spell = new Spell();
        spell.setUrl("static-cantrip");
        spell.setName("Static Cantrip");
        spell.setEnglish("Static Cantrip");
        spell.setLevel(0L);
        spell.setSchool(SpellSchool.builder().school(MagicSchool.EVOCATION).build());
        spell.setDescription("[\"Target takes {@roll 1d6} fire damage.\"]");

        SpellEffect effect = new SpellEffect();
        effect.setDamageFormulas(List.of("1d6@dmg.fire"));
        spell.setEffect(effect);

        var result = mapper.toVttg(spell);

        assertEquals("1d6@dmg.fire", result.getDamageParts().getFirst().getFormula());
        assertNull(result.getCantripScaling());
        assertNull(result.getCantripScalingTiers());
    }

    @Test
    void keepsHealingMarkerInVttgDamageParts() {
        Spell spell = new Spell();
        spell.setUrl("healing-word");
        spell.setName("Healing Word");
        spell.setEnglish("Healing Word");
        spell.setLevel(1L);
        spell.setSchool(SpellSchool.builder().school(MagicSchool.EVOCATION).build());

        SpellEffect effect = new SpellEffect();
        effect.setDamageFormulas(List.of("2Рє4@heal+@mod.spell"));
        spell.setEffect(effect);

        var result = mapper.toVttg(spell);

        // Лечение кодируется токеном @heal в формуле (легаси-флаг isHealing удалён).
        assertEquals("2Рє4@heal+@mod.spell", result.getDamageParts().getFirst().getFormula());
    }

    /**
     * Список заклинаний класса в VTTG строится фильтром по {@code spell.classKeys}
     * (отдельного поля-списка на классе нет). Проверяем, что ключ, который кладёт
     * spell-маппер, ТОЧНО совпадает с {@code key} записи класса — иначе фильтр в
     * {@code WizardStepSpellcasting} не найдёт заклинаний класса.
     */
    @Test
    void spellClassKeysMatchExportedClassKey() {
        CharacterClass wizard = characterClass("wizard", "Волшебник", "Wizard");

        Spell spell = new Spell();
        spell.setUrl("fire-bolt");
        spell.setName("Огненный снаряд");
        spell.setEnglish("Fire Bolt");
        spell.setLevel(0L);
        spell.setSchool(SpellSchool.builder().school(MagicSchool.EVOCATION).build());
        spell.setClassAffiliation(Set.of(wizard));

        List<String> classKeys = mapper.toVttg(spell).getClassKeys();
        String classKey = classMapper.toVttg(wizard).getKey();

        assertEquals(List.of("wizard"), classKeys);
        assertEquals("wizard", classKey);
        assertTrue(classKeys.contains(classKey));
    }

    /** Несколько принадлежностей: канонические ключи, отсортированы и без дублей; неканонические отброшены. */
    @Test
    void spellClassKeysFilterToCanonicalSortedDistinct() {
        Spell spell = new Spell();
        spell.setUrl("cure-wounds");
        spell.setName("Лечение ран");
        spell.setEnglish("Cure Wounds");
        spell.setLevel(1L);
        spell.setSchool(SpellSchool.builder().school(MagicSchool.ABJURATION).build());
        spell.setClassAffiliation(Set.of(
                characterClass("cleric", "Жрец", "Cleric"),
                characterClass("bard", "Бард", "Bard"),
                // Неканонический (хоумбрю) класс отбрасывается фильтром CLASS_KEYS.
                characterClass("blood-hunter", "Охотник на нечисть", "Blood Hunter")));

        assertEquals(List.of("bard", "cleric"), mapper.toVttg(spell).getClassKeys());
    }

    /** Заклинание без принадлежности к классам даёт пустой список ключей (не null). */
    @Test
    void spellWithoutClassAffiliationHasEmptyClassKeys() {
        Spell spell = new Spell();
        spell.setUrl("orphan-spell");
        spell.setName("Ничьё");
        spell.setEnglish("Orphan Spell");
        spell.setLevel(1L);
        spell.setSchool(SpellSchool.builder().school(MagicSchool.EVOCATION).build());

        assertTrue(mapper.toVttg(spell).getClassKeys().isEmpty());
    }

    /**
     * Заклинание, привязанное ТОЛЬКО к подклассу (домен/клятва), попадает в список базового класса:
     * подкласс отображается на ключ родительского класса.
     */
    @Test
    void spellClassKeysIncludeSubclassParent() {
        CharacterClass cleric = characterClass("cleric", "Жрец", "Cleric");
        CharacterClass lifeDomain = characterClass("life-domain", "Домен Жизни", "Life Domain");
        lifeDomain.setParent(cleric);

        Spell spell = new Spell();
        spell.setUrl("bless");
        spell.setName("Благословение");
        spell.setEnglish("Bless");
        spell.setLevel(1L);
        spell.setSchool(SpellSchool.builder().school(MagicSchool.EVOCATION).build());
        spell.setSubclassAffiliation(Set.of(lifeDomain));

        assertEquals(List.of("cleric"), mapper.toVttg(spell).getClassKeys());
    }

    /** Прямая принадлежность и подклассовая объединяются и дедуплицируются (родитель == прямой класс). */
    @Test
    void spellClassKeysMergeClassAndSubclassParentDistinct() {
        CharacterClass wizard = characterClass("wizard", "Волшебник", "Wizard");
        CharacterClass cleric = characterClass("cleric", "Жрец", "Cleric");
        CharacterClass lifeDomain = characterClass("life-domain", "Домен Жизни", "Life Domain");
        lifeDomain.setParent(cleric);
        // Подкласс волшебника: его родитель совпадает с прямой принадлежностью → должен схлопнуться.
        CharacterClass evocation = characterClass("evocation-school", "Школа Воплощения", "Evocation");
        evocation.setParent(wizard);

        Spell spell = new Spell();
        spell.setUrl("combo-spell");
        spell.setName("Combo");
        spell.setEnglish("Combo");
        spell.setLevel(1L);
        spell.setSchool(SpellSchool.builder().school(MagicSchool.EVOCATION).build());
        spell.setClassAffiliation(Set.of(wizard));
        spell.setSubclassAffiliation(Set.of(lifeDomain, evocation));

        assertEquals(List.of("cleric", "wizard"), mapper.toVttg(spell).getClassKeys());
    }

    private CharacterClass characterClass(String url, String name, String english) {
        CharacterClass characterClass = new CharacterClass();
        characterClass.setUrl(url);
        characterClass.setName(name);
        characterClass.setEnglish(english);
        return characterClass;
    }

    @Test
    void mapsBonusActionAndSelfRangeLikeVttgSrd() {
        Spell spell = new Spell();
        spell.setUrl("self-bonus");
        spell.setName("Self Bonus");
        spell.setEnglish("Self Bonus");
        spell.setLevel(1L);
        spell.setSchool(SpellSchool.builder().school(MagicSchool.EVOCATION).build());
        spell.setCastingTime(List.of(SpellCastingTime.of(1L, CastingUnit.BONUS)));
        spell.setRange(List.of(SpellDistance.of(0L, DistanceUnit.SELF)));

        var result = mapper.toVttg(spell);

        assertEquals("bonus", result.getCastingTimeUnit());
        assertEquals("self", result.getRangeUnit());
    }
}

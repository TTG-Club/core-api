package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.beastiary.model.action.AttackType;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VttgSpellMapperTest {
    private final VttgSpellMapper mapper = new VttgSpellMapper(
            new VttgMarkupConverter(new ObjectMapper()),
            new VttgSpellMechanicsExtractor(),
            new VttgSpellScalingExtractor()
    );

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
        effect.setDamageFormula("8к6");
        effect.setDamageTypes(List.of(DamageType.FAIR));
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
        assertEquals("fire", result.getDamageType());
        assertEquals("8к6", result.getDamageFormula());
        assertEquals("dexterity", result.getSaveType());
        assertEquals("half", result.getSaveEffect());
        assertEquals("Первый абзац\n\nВторой абзац", result.getDescription());
        assertEquals("Урон увеличивается на {@roll 1к6} за каждый уровень ячейки выше 3.",
                result.getHigherLevelDescription());
        assertEquals("1к6", result.getScaling().getAdditionalDice());
        assertEquals(result.getHigherLevelDescription(), result.getScaling().getDescription());
        assertEquals("PHB24, p. 241", result.getSource());
        assertEquals("phb24", result.getSourceKey());
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

        var result = mapper.toVttg(spell);

        assertTrue(result.getDescription().startsWith("*Вы бросаете кислотный шарик*"));
        assertTrue(result.getDescription().contains("[сферой](https://ttg.club/glossary/sphere-phb)"));
        assertTrue(result.getDescription().contains("{@roll 1к6}"));
        assertEquals("1к6", result.getDamageFormula());
    }
}

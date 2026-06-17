package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.HealingType;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.model.SpellEffect;
import club.ttg.dnd5.domain.spell.model.enums.SpellSaveEffect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VttgSpellMechanicsExtractorTest {
    private final VttgSpellMechanicsExtractor extractor = new VttgSpellMechanicsExtractor();

    @Test
    void extractsDamageFormulaAndTypeFromText() {
        Spell spell = new Spell();

        var result = extractor.extract(spell,
                "Цель получает {@roll 1к8} урона некротической энергией.");

        assertEquals("1к8", result.damageFormula());
        assertEquals("necrotic", result.damageType());
        assertEquals("1к8@dmg.necrotic", result.damageParts().getFirst().getFormula());
        assertNull(result.isHealing());
    }

    @Test
    void extractsNumericModifierAndNormalizesLatinDiceSeparator() {
        Spell spell = new Spell();

        var result = extractor.extract(spell,
                "При попадании цель получает 2d6 + 3 урона огнём.");

        assertEquals("2к6+3", result.damageFormula());
        assertEquals("fire", result.damageType());
        assertEquals("2к6@dmg.fire+3", result.damageParts().getFirst().getFormula());
    }

    @Test
    void usesDamageFormulaInsteadOfLaterUnrelatedRoll() {
        Spell spell = new Spell();

        var result = extractor.extract(spell,
                "Цель получает {@roll 1к6} урона психической энергией "
                        + "и вычитает {@roll 1к4} из следующего спасброска.");

        assertEquals("1к6", result.damageFormula());
        assertEquals("psychic", result.damageType());
        assertEquals("1к6@dmg.psychic", result.damageParts().getFirst().getFormula());
    }

    @Test
    void extractsHealingAndItsFormulaFromText() {
        Spell spell = new Spell();

        var result = extractor.extract(spell,
                "Существо восстанавливает {@roll 1к8} + ваш модификатор хитов.");

        assertEquals("1к8", result.damageFormula());
        assertEquals("1к8@heal", result.damageParts().getFirst().getFormula());
        assertTrue(result.isHealing());
    }

    @Test
    void treatsHealMarkerFormulaAsHealingDamagePart() {
        Spell spell = new Spell();
        SpellEffect effect = new SpellEffect();
        effect.setDamageFormulas(List.of("2Рє4@heal+@mod.spell"));
        spell.setEffect(effect);

        var result = extractor.extract(spell, "");

        assertEquals("2Рє4@heal+@mod.spell", result.damageParts().getFirst().getFormula());
        assertTrue(result.isHealing());
    }

    @Test
    void treatsTemporaryHealMarkerFormulaAsHealingDamagePart() {
        Spell spell = new Spell();
        SpellEffect effect = new SpellEffect();
        effect.setDamageFormulas(List.of("2Рє4@heal.temp"));
        spell.setEffect(effect);

        var result = extractor.extract(spell, "");

        assertEquals("2Рє4@heal.temp", result.damageParts().getFirst().getFormula());
        assertTrue(result.isHealing());
    }

    @Test
    void usesLegacyHealingTypesWhenFormulaHasNoMarker() {
        Spell spell = new Spell();
        SpellEffect effect = new SpellEffect();
        effect.setDamageFormulas(List.of("2Рє4"));
        effect.setHealingTypes(List.of(HealingType.HEALING));
        spell.setEffect(effect);

        var result = extractor.extract(spell, "");

        assertEquals("2Рє4@heal", result.damageParts().getFirst().getFormula());
        assertTrue(result.isHealing());
    }

    @Test
    void structuredMechanicsHavePriorityOverText() {
        Spell spell = new Spell();
        SpellEffect effect = new SpellEffect();
        effect.setDamageFormulas(List.of("2к6@dmg.cold", "1к6@dmg.radiant"));
        effect.setHealingTypes(List.of(HealingType.HEALING));
        spell.setEffect(effect);

        var result = extractor.extract(spell,
                "Цель получает {@roll 2к6} урона огнём.");

        assertEquals("2к6", result.damageFormula());
        assertEquals("cold", result.damageType());
        assertEquals("2к6@dmg.cold", result.damageParts().get(0).getFormula());
        assertEquals("1к6@dmg.radiant", result.damageParts().get(1).getFormula());
        assertTrue(result.isHealing());
    }

    @Test
    void ignoresRollThatIsNotDamageOrHealing() {
        Spell spell = new Spell();

        var result = extractor.extract(spell,
                "Существо добавляет {@roll 1к4} к любой проверке характеристики.");

        assertNull(result.damageFormula());
        assertNull(result.damageType());
        assertNull(result.damageParts());
        assertNull(result.isHealing());
    }

    @Test
    void doesNotTreatHealingPreventionAsHealing() {
        Spell spell = new Spell();

        var result = extractor.extract(spell,
                "Цель получает {@roll 1к10} урона некротической энергией "
                        + "и не может восстанавливать хиты до конца следующего хода.");

        assertEquals("1к10", result.damageFormula());
        assertEquals("necrotic", result.damageType());
        assertEquals("1к10@dmg.necrotic", result.damageParts().getFirst().getFormula());
        assertNull(result.isHealing());
    }

    @Test
    void extractsHalfDamageOnSuccessfulSaveFromText() {
        Spell spell = new Spell();

        var result = extractor.extract(spell,
                "При успешном спасброске существо получает половину этого урона.");

        assertEquals("half", result.saveEffect());
    }

    @Test
    void structuredSaveEffectHasPriorityOverText() {
        Spell spell = new Spell();
        SpellEffect effect = new SpellEffect();
        effect.setSaveEffect(SpellSaveEffect.SPECIAL);
        spell.setEffect(effect);

        var result = extractor.extract(spell,
                "При успешном спасброске существо получает половину этого урона.");

        assertEquals("special", result.saveEffect());
    }
}

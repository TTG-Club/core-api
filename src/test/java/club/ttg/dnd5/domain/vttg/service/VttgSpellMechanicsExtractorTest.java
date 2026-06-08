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

        assertEquals(List.of("1к8[necrotic]"), result.damageFormulas());
        assertNull(result.isHealing());
    }

    @Test
    void extractsNumericModifierAndNormalizesLatinDiceSeparator() {
        Spell spell = new Spell();

        var result = extractor.extract(spell,
                "При попадании цель получает 2d6 + 3 урона огнём.");

        assertEquals(List.of("2к6+3[fire]"), result.damageFormulas());
    }

    @Test
    void usesDamageFormulaInsteadOfLaterUnrelatedRoll() {
        Spell spell = new Spell();

        var result = extractor.extract(spell,
                "Цель получает {@roll 1к6} урона психической энергией "
                        + "и вычитает {@roll 1к4} из следующего спасброска.");

        assertEquals(List.of("1к6[psychic]"), result.damageFormulas());
    }

    @Test
    void extractsHealingAndItsFormulaFromText() {
        Spell spell = new Spell();

        var result = extractor.extract(spell,
                "Существо восстанавливает {@roll 1к8} + ваш модификатор хитов.");

        assertEquals(List.of("1к8"), result.damageFormulas());
        assertTrue(result.isHealing());
    }

    @Test
    void structuredMechanicsHavePriorityOverText() {
        Spell spell = new Spell();
        SpellEffect effect = new SpellEffect();
        effect.setDamageFormulas(List.of("2к6[cold]", "1к6[radiant]"));
        effect.setHealingTypes(List.of(HealingType.HEALING));
        spell.setEffect(effect);

        var result = extractor.extract(spell,
                "Цель получает {@roll 2к6} урона огнём.");

        assertEquals(List.of("2к6[cold]", "1к6[radiant]"), result.damageFormulas());
        assertTrue(result.isHealing());
    }

    @Test
    void ignoresRollThatIsNotDamageOrHealing() {
        Spell spell = new Spell();

        var result = extractor.extract(spell,
                "Существо добавляет {@roll 1к4} к любой проверке характеристики.");

        assertNull(result.damageFormulas());
        assertNull(result.isHealing());
    }

    @Test
    void doesNotTreatHealingPreventionAsHealing() {
        Spell spell = new Spell();

        var result = extractor.extract(spell,
                "Цель получает {@roll 1к10} урона некротической энергией "
                        + "и не может восстанавливать хиты до конца следующего хода.");

        assertEquals(List.of("1к10[necrotic]"), result.damageFormulas());
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

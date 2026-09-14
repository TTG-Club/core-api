package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.model.DamagePart;
import club.ttg.dnd5.domain.spell.model.SpellEffect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SpellDamageTypesTest {

    @Test
    void addsTypesFromBaseAndCantripTierFormulasAfterManualOnes() {
        DamagePart tierPart = new DamagePart();
        tierPart.setFormula("2к10@dmg.thunder");
        SpellEffect.CantripScalingTier tier = new SpellEffect.CantripScalingTier();
        tier.setLevel(5);
        tier.setParts(List.of(tierPart));

        SpellEffect effect = new SpellEffect();
        effect.setDamageTypes(List.of(DamageType.COLD));
        effect.setDamageFormulas(List.of("3к8@dmg.acid+1к6@DMG.Fire", "1к4@heal", "1к6@dmg.cold"));
        effect.setCantripScalingTiers(List.of(tier));

        SpellDamageTypes.addFromFormulas(effect);

        // Ручной тип остаётся первым, типы формул идут следом без повторов.
        assertEquals(
                List.of(DamageType.COLD, DamageType.ACID, DamageType.FIRE, DamageType.THUNDER),
                effect.getDamageTypes());
    }

    @Test
    void skipsUnknownTagsAndKeepsEmptyFieldUntouched() {
        SpellEffect effect = new SpellEffect();
        effect.setDamageFormulas(List.of("1к6@dmg.plasma", "1к8@heal.temp"));

        SpellDamageTypes.addFromFormulas(effect);

        assertNull(effect.getDamageTypes());
    }

    @Test
    void ignoresMissingEffect() {
        SpellDamageTypes.addFromFormulas(null);
    }
}

package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.model.CreatureAbilities;
import club.ttg.dnd5.domain.beastiary.model.CreatureAbility;
import club.ttg.dnd5.domain.beastiary.model.CreatureInitiative;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreatureInitiativeCalculatorTest {

    @Test
    void bonusIsDexterityModifierWhenMultiplierIsZero() {
        Creature creature = creature(14, (byte) 0, 450L);

        assertEquals(2, CreatureInitiativeCalculator.initiativeBonus(creature));
    }

    @Test
    void bonusAddsProficiencyBonusByExperience() {
        // ЛОВ 18 (+4), опыт 5000 (ПМ 4), множитель 1 → 4 + 4
        Creature creature = creature(18, (byte) 1, 5_000L);

        assertEquals(8, CreatureInitiativeCalculator.initiativeBonus(creature));
    }

    @Test
    void bonusAddsDoubledProficiencyBonusForMultiplierTwo() {
        // ЛОВ 10 (+0), опыт 25000 (ПМ 6), множитель 2 → 0 + 12
        Creature creature = creature(10, (byte) 2, 25_000L);

        assertEquals(12, CreatureInitiativeCalculator.initiativeBonus(creature));
    }

    @Test
    void negativeDexterityModifierIsPreserved() {
        Creature creature = creature(6, (byte) 0, 100L);

        assertEquals(-2, CreatureInitiativeCalculator.initiativeBonus(creature));
    }

    @Test
    void missingAbilitiesAndInitiativeGiveZero() {
        Creature creature = new Creature();

        assertEquals(0, CreatureInitiativeCalculator.initiativeBonus(creature));
    }

    @Test
    void missingExperienceFallsBackToMinimalProficiencyBonus() {
        Creature creature = creature(12, (byte) 1, null);

        // ЛОВ 12 (+1), опыт неизвестен (ПМ 2 по умолчанию), множитель 1 → 1 + 2
        assertEquals(3, CreatureInitiativeCalculator.initiativeBonus(creature));
    }

    private static Creature creature(int dexterity, byte multiplier, Long experience) {
        CreatureAbility dex = new CreatureAbility();
        dex.setValue((short) dexterity);

        CreatureAbilities abilities = new CreatureAbilities();
        abilities.setDexterity(dex);

        CreatureInitiative initiative = new CreatureInitiative();
        initiative.setMultiplier(multiplier);

        Creature creature = new Creature();
        creature.setAbilities(abilities);
        creature.setInitiative(initiative);
        creature.setExperience(experience);
        return creature;
    }
}

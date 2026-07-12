package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.ChallengeRating;

/**
 * Расчёт бонуса инициативы существа: модификатор ЛОВ + бонус мастерства (по опыту) × множитель
 * инициативы из статблока. Единая точка расчёта для карточки существа и трекера инициативы.
 */
public final class CreatureInitiativeCalculator {

    private CreatureInitiativeCalculator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static int initiativeBonus(Creature creature) {
        int mod = 0;
        if (creature.getAbilities() != null && creature.getAbilities().getDexterity() != null) {
            mod = creature.getAbilities().getMod(Ability.DEXTERITY);
        }
        int multiplier = creature.getInitiative() != null ? creature.getInitiative().getMultiplier() : 0;
        if (multiplier == 0) {
            return mod;
        }
        long experience = creature.getExperience() != null ? creature.getExperience() : 0L;
        return mod + ChallengeRating.getPb(experience) * multiplier;
    }
}

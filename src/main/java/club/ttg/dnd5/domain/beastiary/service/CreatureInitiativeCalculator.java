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

    /**
     * Полный бонус инициативы для показа: модификатор ЛОВ плюс {@link #extraBonus(Creature)}.
     */
    public static int initiativeBonus(Creature creature) {
        int mod = 0;
        if (creature.getAbilities() != null && creature.getAbilities().getDexterity() != null) {
            mod = creature.getAbilities().getMod(Ability.DEXTERITY);
        }
        return mod + extraBonus(creature);
    }

    /**
     * Прибавка к инициативе сверх модификатора ЛОВ: бонус мастерства (по опыту) × множитель
     * из статблока. 0, если множителя нет или он равен нулю. Именно её ждёт VTTG в
     * {@code initiativeBonus} — модификатор характеристики система прибавляет сама.
     */
    public static int extraBonus(Creature creature) {
        int multiplier = creature.getInitiative() != null ? creature.getInitiative().getMultiplier() : 0;
        if (multiplier == 0) {
            return 0;
        }
        long experience = creature.getExperience() != null ? creature.getExperience() : 0L;
        return ChallengeRating.getPb(experience) * multiplier;
    }
}

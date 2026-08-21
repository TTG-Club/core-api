package club.ttg.dnd5.domain.feat.model.prerequisite;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Классовое умение, которого требует черта. Из 269 черт прода встречаются только эти
 * четыре: «Использование заклинаний или Магия договора» (черты заклинателей),
 * «Боевой стиль» (боевые стили) и «Оружейные приёмы» (посвящённый рыцаря смерти).
 */
@Getter
@AllArgsConstructor
public enum ClassFeatureRequirement {
    SPELLCASTING("Использование заклинаний"),
    PACT_MAGIC("Магия договора"),
    FIGHTING_STYLE("Боевой стиль"),
    WEAPON_MASTERY("Оружейные приёмы");

    private final String name;
}

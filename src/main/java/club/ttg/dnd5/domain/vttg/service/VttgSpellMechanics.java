package club.ttg.dnd5.domain.vttg.service;

public record VttgSpellMechanics(
        String damageFormula,
        String damageType,
        Boolean isHealing,
        String saveEffect
) {
}

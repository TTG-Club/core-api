package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.vttg.rest.dto.VttgDamagePart;

import java.util.List;

public record VttgSpellMechanics(
        String damageFormula,
        String damageType,
        List<VttgDamagePart> damageParts,
        Boolean isHealing,
        String saveEffect
) {
}

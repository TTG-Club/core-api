package club.ttg.dnd5.domain.vttg.service;

import java.util.List;

public record VttgSpellMechanics(
        List<String> damageFormulas,
        Boolean isHealing,
        String saveEffect
) {
}

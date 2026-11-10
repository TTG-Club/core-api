package club.ttg.dnd5.domain.bastion.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Откуда берутся варианты выбора сооружения, если их не перечисляют вручную.
 */
@Getter
@RequiredArgsConstructor
public enum FacilityChoiceSource {
    /** Инструменты ремесленников из справочника предметов — выбор инструментов мастерской. */
    ARTISAN_TOOLS("Инструменты ремесленников");

    private final String name;
}

package club.ttg.dnd5.domain.spell.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Свойство материального компонента для фильтра, уточняющего «Материальный».
 */
@Getter
@RequiredArgsConstructor
public enum SpellMaterialProperty {
    WITH_COST("С ценой"),
    CONSUMABLE("Расходуемый");

    private final String name;
}

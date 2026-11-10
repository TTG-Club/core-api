package club.ttg.dnd5.domain.spell.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Значение фильтра «Компоненты».
 *
 * <p>«С ценой» и «Расходуемый» уточняют материальный компонент: клиент показывает
 * их только после выбора {@link #MATERIAL}.</p>
 */
@Getter
@RequiredArgsConstructor
public enum SpellComponent {
    VERBAL("Вербальный", false),
    SOMATIC("Соматический", false),
    MATERIAL("Материальный", false),
    WITH_COST("С ценой", true),
    CONSUMABLE("Расходуемый", true);

    private final String name;
    private final boolean materialProperty;
}

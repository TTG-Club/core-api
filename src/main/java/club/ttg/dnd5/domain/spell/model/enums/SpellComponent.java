package club.ttg.dnd5.domain.spell.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Компонент заклинания для фильтра «Компоненты».
 */
@Getter
@RequiredArgsConstructor
public enum SpellComponent {
    VERBAL("Вербальный"),
    SOMATIC("Соматический"),
    MATERIAL("Материальный");

    private final String name;
}

package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CreatureTreasure {
    ANY("Любые"),
    INDIVIDUAL("Индивидуальные"),
    ARCANA("Магия"),
    ARMAMENTS("Вооружение"),
    IMPLEMENTS("Инструменты"),
    RELICS("Реликвии"),
    NONE("Отсутствуют");

    private final String name;
}

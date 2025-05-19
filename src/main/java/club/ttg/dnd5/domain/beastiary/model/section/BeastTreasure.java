package club.ttg.dnd5.domain.beastiary.model.section;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BeastTreasure {
    ANY("Любые"),
    INDIVIDUAL("Индивидуальные"),
    ARCANA("Магия"),
    ARMAMENTS("Вооружение"),
    IMPLEMENTS("Инструменты"),
    RELICS("Реликвии"),
    NONE("Отсутствуют");

    private final String name;
}

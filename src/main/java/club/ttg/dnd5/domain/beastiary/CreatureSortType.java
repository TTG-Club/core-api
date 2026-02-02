package club.ttg.dnd5.domain.beastiary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CreatureSortType {
    CR("Уровень опасности"),
    TYPE("Тип"),
    SIZE("Размер");

    private final String name;
}

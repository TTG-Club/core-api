package club.ttg.dnd5.domain.beastiary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CreatureGroupType {
    CR("Уровень опасности"),
    TYPE("Тип"),
    SIZE("Размер");

    private final String name;
}

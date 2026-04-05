package club.ttg.dnd5.domain.feat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Категории черт */
@Getter
@AllArgsConstructor
public enum FeatCategory {
    ORIGIN("черта происхождения"),
    GENERAL("общая черта"),
    EPIC_BOON("эпическая черта"),
    FIGHTING_STYLE("боевой стиль"),
    DRAGONMARK("метка дракона"),
    PATH_OF_THE_DEATH_KNIGHT("Черта пути рыцаря смерти"),
    PATH_OF_THE_LICH("Черта пути лича");

    private final String name;
}

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
    DRAGONMARK("метка дракона");

    private final String name;
}

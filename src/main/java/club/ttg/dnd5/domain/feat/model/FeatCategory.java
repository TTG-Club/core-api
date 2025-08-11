package club.ttg.dnd5.domain.feat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Категории черт */
@Getter
@AllArgsConstructor
public enum FeatCategory {
    ORIGIN("Черта происхождения"),
    GENERAL("Общая черта"),
    EPIC_BOON("Эпическая черта"),
    FIGHTING_STYLE("Боевой стиль") ;

    private final String name;
}

package club.ttg.dnd5.domain.feat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Категории черт */
@Getter
@AllArgsConstructor
public enum FeatCategory {
    ORIGIN("черты происхождения"),
    GENERAL("общие черты"),
    EPIC_BOON("эпические черты"),
    FIGHTING_STYLE("боевые стили") ;

    private final String name;
}

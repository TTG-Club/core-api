package club.ttg.dnd5.dictionary.character;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeatType {
    ORIGIN("первоначальные черты"),
    GENERAL("основные черты"),
    EPIC_BOON("эпические черты"),
    FIGHTING_STYLE("боевые стили") ;

    private final String cyrillicName;
}

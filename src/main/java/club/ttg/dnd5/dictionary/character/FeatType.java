package club.ttg.dnd5.dictionary.character;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeatType {
    ORIGIN("наследия"),
    GENERAL("основные"),
    EPIC("эпические черты"),
    FIGHTING_STYLE("боевые стили");

    private final String сyrillicNane;
}

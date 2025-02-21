package club.ttg.dnd5.model.spell.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Deprecated
@Getter
@AllArgsConstructor
public enum SpellDistance {
    SELF("на себя", "на себя"),
    TOUCH("касание", "при касании"),
    TEN_FEET("10 футов", "в пределах 10 футов"),
    THIRTY_FEET("30 футов", "в пределах 30 футов"),
    SIXTY_FEET("60 футов", "в пределах 60 футов"),
    NINETY_FEET("90 футов", "в пределах 90 футов"),
    ONE_HUNDRED_TWENTY_FEET("120 футов", "в пределах 120 футов"),
    THREE_HUNDRED_FEET("300 футов", "в пределах 300 футов"),
    FIVE_HUNDRED_FEET("500 футов", "в пределах 500 футов"),
    ONE_MILE("1 миля", "в пределах 1 мили"),
    UNLIMITED("неограниченно", "в любом месте"),
    SPECIAL("особая", "в зависимости от заклинания"),
    SIGHT("в пределах видимости", "в пределах видимости"),
    UNLIMITED_PLANE("неограниченно в пределах плана", "в любом месте плана");

    private final String distance;         // Название дистанции (для вывода)
    private final String context;  // Описание в контексте

    public String getFormattedName(int number) {
        return number == 1 ? distance : context;
    }
}

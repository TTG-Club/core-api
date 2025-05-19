package club.ttg.dnd5.domain.beastiary.model.language;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Language {
    // Стандартные языки
    COMMON("общий"),
    COMMON_SIGN_LANGUAGE("общий язык жестов"),
    DRACONIC("драконий"),
    DWARVISH("дварфский"),
    ELVISH("эльфийский"),
    GIANT("великаний"),
    GNOMISH("гномий"),
    GOBLIN("гоблинский"),
    HALFLING("полуросликов"),
    ORC("орочий"),

    // Редкие языки
    ABYSSAL("бездны"),
    Celestial("небесный"),
    DEEP("глубинная речь"),
    DRUIDIC("друидический"),
    INFERNAL("инфернальный"),
    PRIMORDIAL("Первичный*"),
    SYLVAN("сильван"),
    THIEVES("воровской жаргон"),
    UNDERCOMMON("подземный");

    private final String name;
}

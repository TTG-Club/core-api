package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Language {
    // Стандартные языки
    COMMON("общий", "распространенный"),
    COMMON_SIGN_LANGUAGE("общий язык жестов", "распространенный"),
    DRACONIC("драконий", "распространенный"),
    DWARVISH("дварфский", "распространенный"),
    ELVISH("эльфийский", "распространенный"),
    GIANT("великаний", "распространенный"),
    GNOMISH("гномий", "распространенный"),
    GOBLIN("гоблинский", "распространенный"),
    HALFLING("полуросликов", "распространенный"),
    ORC("орочий", "распространенный"),

    // Редкие языки
    ABYSSAL("бездны", "редкий"),
    Celestial("небесный", "редкий"),
    DEEP("глубинная речь", "редкий"),
    DRUIDIC("друидический", "редкий"),
    INFERNAL("инфернальный", "редкий"),
    PRIMORDIAL("первичный", "редкий"),
    SYLVAN("сильван", "редкий"),
    THIEVES("воровской жаргон", "редкий"),
    UNDERCOMMON("подземный", "редкий");

    private final String name;
    private final String type;
}

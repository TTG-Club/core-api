package club.ttg.dnd5.domain.bastion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Требование для возведения специализированного сооружения (помимо уровня).
 *
 * <p>Разобрано в перечисление, чтобы мини-игра могла проверить его по листу персонажа;
 * отсутствие требования — {@code null} в сооружении.</p>
 */
@Getter
@AllArgsConstructor
public enum FacilityPrerequisite {
    ARCANE_FOCUS_OR_TOOL("Умение использовать заклинательную фокусировку или инструмент в качестве неё"),
    SPELLCASTING_FOCUS("Умение использовать заклинательную фокусировку"),
    HOLY_SYMBOL_OR_DRUIDIC_FOCUS("Умение использовать священный символ или фокусировку друида в качестве заклинательной фокусировки"),
    EXPERTISE("Компетентность в любом навыке"),
    FIGHTING_STYLE_OR_UNARMORED_DEFENSE("Умение Боевой стиль или Защита без доспехов"),
    CULT_OF_THE_DRAGON_MEMBERSHIP("Членство в Культе Дракона"),
    EMERALD_ENCLAVE_MEMBERSHIP("Членство в Изумрудном Анклаве"),
    HARPERS_MEMBERSHIP("Членство в Арфистах"),
    LORDS_ALLIANCE_MEMBERSHIP("Членство в Союзе лордов"),
    ORDER_OF_THE_GAUNTLET_MEMBERSHIP("Членство в Ордене Перчатки"),
    PURPLE_DRAGON_KNIGHTS_MEMBERSHIP("Членство в Рыцарях Пурпурного дракона"),
    RED_WIZARDS_MEMBERSHIP_AND_SPELLCASTING_FOCUS(
            "Членство в Красных Волшебниках, умение использовать заклинательную фокусировку"),
    ZHENTARIM_MEMBERSHIP("Членство в Жентариме"),
    MEDICINE_PROFICIENCY("Владение навыком Медицина"),
    HOUSE_ORIEN_RENOWN_15("Известность 15+ в Доме Ориен"),
    HOUSE_LYRANDAR_RENOWN_25("Известность 25+ в Доме Лирандар"),
    HOUSE_KUNDARAK_RENOWN_15("Известность 15+ в Доме Кундарак"),
    DRAGONMARKED_HOUSE_RENOWN_10("Известность 10+ в любом меченом доме"),
    ARTISAN_TOOLS_FOCUS("Умение использовать инструменты ремесленника в качестве заклинательной фокусировки"),
    SHELTERING_HANDS_MEMBERSHIP("Членство в Укрывающих руках"),
    SEERS_OF_SEA_AND_SKY_MEMBERSHIP("Членство в Провидцах моря и неба"),
    PHANTASMIC_CIRCUS_MEMBERSHIP("Членство в Призрачном цирке"),
    NINTH_QUILL_MEMBERSHIP("Членство в Девятом пере"),
    HORIZON_WEAVERS_MEMBERSHIP_AND_FOCUS("Членство в Ткачах горизонта, умение использовать магическую "
            + "фокусировку, священный символ или инструмент в качестве заклинательной фокусировки"),
    CRUCIBLE_KEEPERS_MEMBERSHIP("Членство в Хранителях горнила"),
    COVENANT_OF_THE_GRAVE_MEMBERSHIP("Членство в Завете могилы"),
    BRINGERS_OF_COSMIC_DAWN_MEMBERSHIP("Членство в Вестниках космического рассвета"),
    BEJEWELED_CONCLAVE_MEMBERSHIP("Членство в Драгоценном конклаве");

    private final String name;
}

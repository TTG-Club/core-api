package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Места обитания
 */
@Getter
@AllArgsConstructor
public enum Habitat {
    ANY("Любая"),
    ARCTIC("Арктика"),
    COASTAL("Побережье"),
    DESERT("Пустыня"),
    FOREST("Лес"),
    GRASSLAND("Луг"),
    HILL("Холмы"),
    MOUNTAIN("Горы"),
    PLANAR_ABYSS("План (Бездна)"),
    PLANAR_ACHERON("План (Ахерон)"),
    PLANAR_ELEMENTAL_PLANE_OF_AIR("План (Стихийный план воздуха)"),
    PLANAR_ARBOREA("План (Арборея)"),
    PLANAR_ARCADIA("План (Аркадия)"),
    PLANAR_ASTRAL_PLANE("План (астральный план)"),
    PLANAR_BEASTLAND("План (Звериные земли)"),
    PLANAR_BYTOPIA("План (Битопия)"),
    PLANAR_CARCERY("План (Карцери)"),
    PLANAR_ELEMENTAL_PLANE_OF_EARTH("План (Стихийный план земли)"),
    PLANAR_ELEMENTAL_CHAOS("План (Стихийный хаос)"),
    PLANAR_ELYSIUM("План (Элизиум)"),
    PLANAR_ETHEREAL_PLANE("План (Эфирный план)"),
    PLANAR_FEYWILD("План (Страна фей)"),
    PLANAR_ELEMENTAL_PLANE_OF_FIRE("план (Стихийный план огня)"),
    PLANAR_GEHENNA("План (Гиена)"),
    PLANAR_HADES("План (Гадес)"),
    PLANAR_LIMBO("План (Лимбо)"),
    PLANAR_LOWER_PLANES("План (нижние планы)"),
    PLANAR_MECHANUS("План (Механус)"),
    PLANAR_MOUNT_CELESTIA("План (Гора Селестия)"),
    PLANAR_NINE_HELLS("План (Девять преисподних)"),
    PLANAR_PANDEMONIUM("План (Пандемониум)"),
    PLANAR_SHADOWFELL("План (Царство теней)"),
    PLANAR_ELEMENTAL_PLANE_OF_WATER("план (Стихийный план воздуха)"),
    PLANAR_YSGARD("План (Асгард)"),
    SWAMP("Болота"),
    UNDERDARK("Подземье"),
    UNDERWATER("Под водой"),
    URBAN("Город");

    private final String name;
}

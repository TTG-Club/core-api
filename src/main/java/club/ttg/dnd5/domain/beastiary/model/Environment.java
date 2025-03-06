package club.ttg.dnd5.domain.beastiary.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Environment {
    ARCTIC("полярная тундра"),
    COAST("побережье"),
    WATERS("под водой"),
    GRASSLAND("равнина/луг"),
    UNDERGROUND("подземье"),
    CITY("город"),
    VILLAGE("деревня"),
    RUINS("руины"),
    DUNGEON("подземелья"),
    FOREST("лес"),
    HILL("холмы"),
    MOUNTAIN("горы"),
    SWAMP("болото"),
    DESERT("пустыня");

    private final String name;
}

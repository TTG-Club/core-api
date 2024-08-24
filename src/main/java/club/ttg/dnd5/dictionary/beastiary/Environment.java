package club.ttg.dnd5.dictionary.beastiary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Environment {
    ARCTIC("полярная тундра", "arctic"),
    COAST("побережье", "coastal"),
    WATERS("под водой", "underwater"),
    GRASSLAND("равнина/луг", "grassland"),
    UNDERGROUND("подземье", "underdark"),
    CITY("город", "urban"),
    VILLAGE("деревня", null),
    RUINS("руины", null),
    DUNGEON("подземелья", null),
    FOREST("лес", "forest"),
    HILL("холмы", "hill"),
    MOUNTAIN("горы", "mountain"),
    SWAMP("болото", "swamp"),
    DESERT("пустыня", "desert"),
    TROPICS("тропики", null);

    private String name;
    private String xmlName;
}

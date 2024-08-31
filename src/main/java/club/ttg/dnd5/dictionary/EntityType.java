package club.ttg.dnd5.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EntityType {
    CLASS ("class"),
    SPECIE ("specie"),
    BACKGROUND("background"),
    FEAT("feat"),
    SPELL("spell"),
    CREATURE("creature"),
    EQUIPMENT("equipment"),
    FEATURE("feature"),
    ;

    final String name;
}

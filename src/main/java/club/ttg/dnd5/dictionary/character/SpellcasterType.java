package club.ttg.dnd5.dictionary.character;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SpellcasterType {
    FULL(9),
    HALF(5),
    PARTLY(4),
    NONE(0);

    private final int maxSpellLevel;
}

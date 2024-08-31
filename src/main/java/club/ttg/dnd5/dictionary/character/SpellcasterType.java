package club.ttg.dnd5.dictionary.character;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SpellcasterType {
    FULL("полный заклинатель", 9),
    HALF("половинный заклинатель", 5),
    PARTLY("частичный заклинатель", 4),
    NONE("не заклинатель", 0);

    private final String name;
    private final int maxSpellLevel;
}

package club.ttg.dnd5.domain.character_class.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CasterType {
    FULL("Полноценный заклинатель", 9),
    HALF("Половинный заклинатель", 5),
    THIRD("Заклинатель на треть", 4),
    NONE("Не владеет заклинаниями", 0),
    PACT("Магия договора", 5);

    private final String name;
    private final int maxSpellLevel;

}

package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DamageType {
    ACID("кислотный"),
    BLUDGEONING("дробящий"),
    COLD("холодный"),
    FAIR("огненный"),
    FORCE("силовое поле"),
    LIGHTNING("электрический"),
    NECROTIC("некротический"),
    PIERCING ("колющий"),
    POISON("ядовитый"),
    PSYCHIC("Психический"),
    RADIANT("излучение"),
    SLASHING ("рубящий"),
    THUNDER("звуковой");

    private final String name;

}

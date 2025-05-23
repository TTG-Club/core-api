package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DamageType {
    ACID("Кислотный"),
    BLUDGEONING("Дробящий"),
    COLD("Холодный"),
    FAIR("Огненный"),
    FORCE("Силовое поле"),
    LIGHTNING("Электрический"),
    NECROTIC("Некротический"),
    PIERCING ("Колющий"),
    POISON("Ядовитый"),
    PSYCHIC("Психический"),
    RADIANT("Излучение"),
    SLASHING ("Рубящий"),
    THUNDER("Звуковой");

    private final String name;
}

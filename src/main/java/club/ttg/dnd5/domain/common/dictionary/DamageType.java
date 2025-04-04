package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DamageType {
    FAIR("огонь", "огонь"),
    COLD("холод", "холод"),
    LIGHTNING("электричество","электричество"),
    POISON("яд","яд"),
    ACID("кислота","кислота"),
    THUNDER("звук","звук"),
    NECROTIC("некротическая энергия","некротическая энергия"),
    PSYCHIC("психическая энергия", "психическая энергия"),
    RADIANT("излучение","излучение"),
    FORCE("силовое поле","силовое поле"),

    BLUDGEONING("дробящий","дробящий"),
    PIERCING ("колющий","колющий"),
    SLASHING ("рубящий","рубящий"),

    PHYSICAL("дробящий, колющий и рубящий урон от немагических атак", "физический"),
    SILVER("дробящий, колющий и рубящий урон от немагических атак, а также от немагического оружия, которое при этом не посеребрено", "физический и не посеребрённое"),
    ADAMANT("дробящий, колющий и рубящий урон от немагических атак, а также от немагического оружия, которое при этом не изготовлено из адамантина", "физический и не адамантиновое"),
    NO_DAMAGE("без урона","без урона"),

    PHYSICAL_MAGIC("дробящий, колющий и рубящий урон от магического оружия", "физический магический"),
    PIERCING_GOOD("колющий от магического оружия, используемого добрыми существами", "колющий магический (добро)"),
    MAGIC("урон от заклинаний", "урон от заклинаний"),
    DARK("дробящий, колющий и рубящий, пока находится в области тусклого света или тьмы", "физический в тусклом свете или тьме"),
    METAL_WEAPON("дробящий, колющий и рубящий урон от оружия из металла", "физический только металл"),
    VORPAL_SWORD("рубящий удар мечом головорубом", "рубящий головоруб");

    private final String cyrillicName;
    private final String shortName;
}

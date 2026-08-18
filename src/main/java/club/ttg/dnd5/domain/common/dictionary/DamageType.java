package club.ttg.dnd5.domain.common.dictionary;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DamageType {
    ACID("Кислотный"),
    BLUDGEONING("Дробящий"),
    COLD("Холодный"),
    /**
     * Огненный урон. {@code FAIR} — историческая опечатка в имени константы; значение
     * принимается на входе, чтобы не ломались сохранённые фильтры и старые ссылки.
     * Алиас можно убрать, когда такие ссылки перестанут встречаться.
     */
    @JsonAlias("FAIR")
    FIRE("Огненный"),
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

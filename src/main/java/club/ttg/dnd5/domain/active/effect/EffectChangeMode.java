package club.ttg.dnd5.domain.active.effect;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EffectChangeMode {
    ADDITION("Добавление"),
    DECREASE("Понижение"),
    INCREASE("Повышение"),
    REWRITE("Перезапись"),
    ;
    private final String name;
}

package club.ttg.dnd5.domain.common.model.mechanics;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Вид защиты от урона.
 *
 * <p>Три исхода одного и того же выбора типа урона: сопротивление делит урон пополам,
 * иммунитет отменяет его целиком, уязвимость удваивает. Заводится отдельным словарём, а
 * не тремя наборами, потому что у выбора игрока набор ещё не известен — известен только
 * исход (см. {@link DamageDefenseFromChoice}).</p>
 */
@Getter
@AllArgsConstructor
public enum DamageDefenseKind {
    RESISTANCE("сопротивление"),
    IMMUNITY("иммунитет"),
    VULNERABILITY("уязвимость");

    private final String name;
}

package club.ttg.dnd5.domain.beastiary.model.action;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AttackType {
    MELEE("Рукопашная атака"),
    RANGE("Дальнобойная атака"),
    MELEE_OR_RANGE("Рукопашная или дальнобойная атака");

    private final String name;
}

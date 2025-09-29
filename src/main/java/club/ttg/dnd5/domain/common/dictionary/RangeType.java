package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RangeType {
    MELE("рукопашная"),
    RANGED("дальнобойная");

    private final String name;
}

package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Delimiter {
    AND("и"),
    OR("или");

    private final String name;
}

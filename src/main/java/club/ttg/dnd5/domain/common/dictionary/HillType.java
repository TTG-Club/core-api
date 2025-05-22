package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HillType {
    HIT("хиты"),
    TEMPORARY_HIT("временные хиты");
    private final String name;
}

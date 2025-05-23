package club.ttg.dnd5.domain.common.dictionary;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum HealingType {
    HEALING("Хиты"),
    TEMPORARY_HITPOINTS("Временные хиты");

    private final String name;
}

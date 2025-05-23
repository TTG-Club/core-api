package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SenseType {
    DARKVISION("тёмное зрение"),
    BLINDSIGHT("слепое зрение"),
    TRUESIGHT("истинное зрение"),
    TREMORSENSE("чувство вибрации");

    private final String name;
}

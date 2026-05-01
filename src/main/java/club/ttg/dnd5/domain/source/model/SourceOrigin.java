package club.ttg.dnd5.domain.source.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SourceOrigin {
    OFFICIAL("Официальные источники", "Basic"),
    THIRD_PARTY("Контент от третьих лиц", "3rd"),
    HOMEBREW("Самоделка", "HB");

    private final String name;
    private final String label;
}

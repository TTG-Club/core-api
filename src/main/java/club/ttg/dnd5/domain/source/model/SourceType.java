package club.ttg.dnd5.domain.source.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@Deprecated(forRemoval = true)
public enum SourceType {
    OFFICIAL("Базовые", "Официальные источники", "Basic"),
    SETTING("Сеттинги", "Официальные источники", "Basic"),
    MODULE("Приключения",  "Официальные источники", "Basic"),
    TEST("Неизведанная аркана", "Тестовый материал", "UA"),
    THIRD_PARTY("3rd party",  "Контент от третьих лиц", "3rd"),
    CUSTOM("Homebrew", "Самоделка", "HB");

    private final String name;
    private final String group;
    private final String label;

    public SourceOrigin toOrigin() {
        return switch (this) {
            case THIRD_PARTY -> SourceOrigin.THIRD_PARTY;
            case CUSTOM -> SourceOrigin.HOMEBREW;
            default -> SourceOrigin.OFFICIAL;
        };
    }

    public SourceKind toKind() {
        return switch (this) {
            case SETTING -> SourceKind.SETTING;
            case MODULE -> SourceKind.ADVENTURE;
            default -> SourceKind.SOURCEBOOK;
        };
    }

    public static SourceType from(SourceOrigin origin, SourceKind kind) {
        if (origin == SourceOrigin.THIRD_PARTY) {
            return THIRD_PARTY;
        }
        if (origin == SourceOrigin.HOMEBREW) {
            return CUSTOM;
        }
        return switch (kind) {
            case SETTING -> SETTING;
            case ADVENTURE -> MODULE;
            case SOURCEBOOK -> OFFICIAL;
        };
    }
}

package club.ttg.dnd5.domain.source.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Степень официальности источника: кто выпустил материал.
 * Порядок значений — порядок групп в фильтре источников и сортировки.
 */
@AllArgsConstructor
@Getter
@ToString
public enum SourceOfficiality {
    OFFICIAL("Официальные", "Официальные источники", "Basic"),
    TEST("Неизведанная аркана", "Тестовый материал", "UA"),
    THIRD_PARTY("3rd party", "Контент от третьих лиц", "3rd"),
    HOMEBREW("Homebrew", "Самоделка", "HB");

    /** Название группы в фильтре источников */
    private final String name;
    /** Название группы источника в ответах ({@code source.group.name}) */
    private final String group;
    /** Короткая метка группы ({@code source.group.label}) */
    private final String label;
}

package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.beastiary.model.sense.CreatureSenses;
import club.ttg.dnd5.domain.common.dictionary.Alignment;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.Habitat;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import club.ttg.dnd5.dto.base.filters.QuerySingleton;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO запроса фильтрации существ через URL-параметры.
 * <p>
 * Пример: {@code /search?cr=1,2,3&type=beast,dragon&type_mode=1&type_union=1&source=DMG,MM}
 */
@Data
@NoArgsConstructor
public class CreatureQueryRequest
{
    /** Текстовый поиск. */
    private String search;

    /** Уровень опасности (по experience). */
    private QueryFilter<Long> cr;

    /** Тип существа (JSONB types->values). */
    private QueryFilter<CreatureType> type;

    /** Размер (JSONB sizes->values). */
    private QueryFilter<Size> size;

    /** Мировоззрение (enum STRING column). */
    private QueryFilter<Alignment> alignment;

    /** Место обитания (JSONB section->habitats). */
    private QueryFilter<Habitat> habitat;

    /** Чувства (JSONB senses). */
    private QueryFilter<CreatureSenses> senses;

    /** Умения — SHA-256 хэши (JSONB traits[].name). */
    private QueryFilter<String> traits;

    /** Тег типа — SHA-256 хэши (JSONB types->text + name ILIKE). */
    private QueryFilter<String> tag;

    /** Логово (1=include, 0=exclude). */
    private QuerySingleton lair;

    /** Легендарное действие (1=include, 0=exclude). */
    private QuerySingleton legendaryAction;

    /** Источники — акронимы (только include, без _mode). */
    private Set<String> source = Set.of();

    /** Номер страницы (0-based). */
    private int page = 0;

    /** Размер страницы. */
    private int pageSize = 10000;
}

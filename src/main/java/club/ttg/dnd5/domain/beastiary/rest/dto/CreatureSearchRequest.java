package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.beastiary.model.sense.CreatureSenses;
import club.ttg.dnd5.domain.common.dictionary.Alignment;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.Habitat;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.dto.base.filters.AbstractSearchRequest;
import club.ttg.dnd5.dto.base.filters.ThreeStateFilter;
import club.ttg.dnd5.dto.base.filters.ThreeStateSingleton;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO запроса фильтрации существ (бестиарий).
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreatureSearchRequest extends AbstractSearchRequest
{
    /** Тип существа (3-state, JSONB types->values). */
    private ThreeStateFilter<CreatureType> creatureType;

    /** Размер (3-state, JSONB sizes->values). */
    private ThreeStateFilter<Size> creatureSize;

    /** Мировоззрение (3-state, enum STRING). */
    private ThreeStateFilter<Alignment> alignment;

    /** Уровень опасности по опыту (3-state). */
    private ThreeStateFilter<Long> challengeRating;

    /** Место обитания (3-state, JSONB section->habitats). */
    private ThreeStateFilter<Habitat> habitat;

    /** Чувства (3-state, JSONB senses). */
    private ThreeStateFilter<CreatureSenses> senses;

    /** Умения (3-state, JSONB traits[].name). */
    private ThreeStateFilter<String> traits;

    /** Тег типа (3-state, JSONB types->text + name ILIKE). */
    private ThreeStateFilter<String> tag;

    /** Логово (3-state singleton). */
    private ThreeStateSingleton lair;

    /** Легендарное действие (3-state singleton). */
    private ThreeStateSingleton legendaryAction;
}

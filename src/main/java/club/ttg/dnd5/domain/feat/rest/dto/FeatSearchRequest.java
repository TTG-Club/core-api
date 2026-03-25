package club.ttg.dnd5.domain.feat.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.dto.base.filters.AbstractSearchRequest;
import club.ttg.dnd5.dto.base.filters.ThreeStateFilter;
import club.ttg.dnd5.dto.base.filters.ThreeStateSingleton;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO запроса фильтрации черт.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FeatSearchRequest extends AbstractSearchRequest
{
    /** Категория черты (3-state enum). */
    private ThreeStateFilter<FeatCategory> category;

    /** Улучшаемые характеристики (3-state, JSONB). */
    private ThreeStateFilter<Ability> ability;

    /** Повторяемость (3-state singleton). */
    private ThreeStateSingleton repeatability;
}

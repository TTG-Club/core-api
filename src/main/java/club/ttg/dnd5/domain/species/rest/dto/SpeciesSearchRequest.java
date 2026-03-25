package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.dto.base.filters.AbstractSearchRequest;
import club.ttg.dnd5.dto.base.filters.ThreeStateFilter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO запроса фильтрации видов (рас).
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SpeciesSearchRequest extends AbstractSearchRequest
{
    /** Тип существа (3-state enum). */
    private ThreeStateFilter<CreatureType> creatureType;
}

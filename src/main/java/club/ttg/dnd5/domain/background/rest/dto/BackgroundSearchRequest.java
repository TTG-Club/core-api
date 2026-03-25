package club.ttg.dnd5.domain.background.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.dto.base.filters.AbstractSearchRequest;
import club.ttg.dnd5.dto.base.filters.ThreeStateFilter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO запроса фильтрации предысторий.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BackgroundSearchRequest extends AbstractSearchRequest
{
    /** Характеристики (3-state, JSONB). */
    private ThreeStateFilter<Ability> ability;

    /** Навыки (3-state, JSONB). */
    private ThreeStateFilter<Skill> skill;
}

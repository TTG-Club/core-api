package club.ttg.dnd5.domain.background.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.filter.rest.FilterParam;
import club.ttg.dnd5.dto.base.filters.AbstractQueryRequest;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BackgroundQueryRequest extends AbstractQueryRequest
{
    @FilterParam(enumClass = Ability.class)
    private QueryFilter<Ability> ability;

    @FilterParam(enumClass = Skill.class)
    private QueryFilter<Skill> skill;
}

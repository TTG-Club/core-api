package club.ttg.dnd5.domain.feat.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.domain.filter.rest.FilterParam;
import club.ttg.dnd5.dto.base.filters.AbstractQueryRequest;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import club.ttg.dnd5.dto.base.filters.QuerySingleton;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FeatQueryRequest extends AbstractQueryRequest
{
    @FilterParam(enumClass = FeatCategory.class)
    private QueryFilter<FeatCategory> category;

    @FilterParam(enumClass = Ability.class)
    private QueryFilter<Ability> ability;

    @FilterParam
    private QuerySingleton repeatability;
}

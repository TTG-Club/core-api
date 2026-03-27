package club.ttg.dnd5.domain.feat.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import club.ttg.dnd5.dto.base.filters.QuerySingleton;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class FeatQueryRequest
{
    private String search;
    private QueryFilter<FeatCategory> category;
    private QueryFilter<Ability> ability;
    private QuerySingleton repeatability;
    private Set<String> source = Set.of();
    private int page = 0;
    private int pageSize = 10000;
}

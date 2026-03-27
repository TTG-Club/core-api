package club.ttg.dnd5.domain.background.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class BackgroundQueryRequest
{
    private String search;
    private QueryFilter<Ability> ability;
    private QueryFilter<Skill> skill;
    private Set<String> source = Set.of();
    private int page = 0;
    private int pageSize = 10000;
}

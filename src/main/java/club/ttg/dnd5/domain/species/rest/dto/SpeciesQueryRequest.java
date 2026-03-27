package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class SpeciesQueryRequest
{
    private String search;
    private QueryFilter<CreatureType> creatureType;
    private Set<String> source = Set.of();
    private int page = 0;
    private int pageSize = 10000;
}

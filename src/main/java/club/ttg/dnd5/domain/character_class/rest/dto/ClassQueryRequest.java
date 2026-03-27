package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class ClassQueryRequest
{
    private String search;
    private QueryFilter<Dice> hitDice;
    private Set<String> source = Set.of();
    private int page = 0;
    private int pageSize = 10000;
}

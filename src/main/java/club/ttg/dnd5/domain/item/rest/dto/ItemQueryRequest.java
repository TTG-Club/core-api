package club.ttg.dnd5.domain.item.rest.dto;

import club.ttg.dnd5.domain.item.model.ItemType;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class ItemQueryRequest
{
    private String search;
    private QueryFilter<ItemType> itemType;
    private Set<String> source = Set.of();
    private int page = 0;
    private int pageSize = 10000;
}

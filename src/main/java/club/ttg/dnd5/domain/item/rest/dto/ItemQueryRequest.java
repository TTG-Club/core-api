package club.ttg.dnd5.domain.item.rest.dto;

import club.ttg.dnd5.domain.filter.rest.FilterParam;
import club.ttg.dnd5.domain.item.model.ItemType;
import club.ttg.dnd5.dto.base.filters.AbstractQueryRequest;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ItemQueryRequest extends AbstractQueryRequest
{
    @FilterParam(value = "itemType", enumClass = ItemType.class)
    private QueryFilter<ItemType> itemType;
}

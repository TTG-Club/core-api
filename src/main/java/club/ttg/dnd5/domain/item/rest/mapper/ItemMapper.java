package club.ttg.dnd5.domain.item.rest.mapper;

import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.rest.dto.ItemDetailResponse;
import club.ttg.dnd5.domain.item.rest.dto.ItemRequest;
import club.ttg.dnd5.domain.item.rest.dto.ItemShortResponse;

public interface ItemMapper {
    ItemDetailResponse toDetailDto(final Item item);

    ItemShortResponse toShortDto(Item item);

    Item toEntity(ItemRequest itemDto);
}

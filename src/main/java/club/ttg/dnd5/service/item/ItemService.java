package club.ttg.dnd5.service.item;

import club.ttg.dnd5.dto.item.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto getItem(String itemUtl);

    Collection<ItemDto> getItems();

    ItemDto addItem(ItemDto itemDto);

    ItemDto updateItem(String itemUrl, ItemDto itemDto);

    ItemDto delete(String itemUrl);

    boolean existsByUrl(String url);
}

package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.item.rest.dto.ItemDetailResponse;
import club.ttg.dnd5.domain.item.rest.dto.ItemRequest;
import club.ttg.dnd5.domain.item.rest.dto.ItemShortResponse;

import java.util.Collection;

public interface ItemService {
    ItemDetailResponse getItem(String itemUtl);

    Collection<ItemShortResponse> getItems();

    ItemDetailResponse addItem(ItemRequest itemDto);

    ItemDetailResponse updateItem(String itemUrl, ItemRequest itemDto);

    ItemShortResponse delete(String itemUrl);

    boolean existsByUrl(String url);
}

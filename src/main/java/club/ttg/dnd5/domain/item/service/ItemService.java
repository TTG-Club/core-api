package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.item.rest.dto.ItemDetailResponse;
import club.ttg.dnd5.domain.item.rest.dto.ItemRequest;
import club.ttg.dnd5.domain.item.rest.dto.ItemShortResponse;

import java.util.Collection;

public interface ItemService {
    ItemDetailResponse getItem(String itemUtl);

    Collection<ItemShortResponse> getItems(String searchLine, final SearchBody searchBody);

    Item addItem(ItemRequest itemDto);

    String updateItem(String itemUrl, ItemRequest itemDto);

    String deleteItem(String itemUrl);

    boolean existOrThrow(String url);

    ItemRequest findFormByUrl(String url);

    ItemDetailResponse preview(ItemRequest request);
}

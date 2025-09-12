package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.common.rest.dto.PageResponse;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.item.rest.dto.ItemDetailResponse;
import club.ttg.dnd5.domain.item.rest.dto.ItemRequest;
import club.ttg.dnd5.domain.item.rest.dto.ItemShortResponse;

public interface ItemService {
    ItemDetailResponse getItem(String itemUtl);

    PageResponse<ItemShortResponse> getItems(String searchLine, final int page, final int limit, final String[] sort, final SearchBody searchBody);

    String addItem(ItemRequest itemDto);

    String updateItem(String itemUrl, ItemRequest itemDto);

    String delete(String itemUrl);

    boolean existOrThrow(String url);

    ItemRequest findFormByUrl(String url);

    ItemDetailResponse preview(ItemRequest request);
}

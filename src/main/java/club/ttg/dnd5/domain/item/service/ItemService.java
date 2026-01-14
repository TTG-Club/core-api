package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.item.rest.dto.ItemDetailResponse;
import club.ttg.dnd5.domain.item.rest.dto.ItemRequest;
import club.ttg.dnd5.domain.item.rest.dto.ItemShortResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.Collection;

public interface ItemService {
    ItemDetailResponse getItem(String itemUtl);

    Collection<ItemShortResponse> getItems(String searchLine, final SearchBody searchBody);

    String addItem(ItemRequest itemDto);

    String updateItem(String itemUrl, ItemRequest itemDto);

    String delete(String itemUrl);

    boolean existOrThrow(String url);

    ItemRequest findFormByUrl(String url);

    ItemDetailResponse preview(ItemRequest request);

    Collection<ItemShortResponse> getItems(@Valid @Size(min = 2) String searchLine, String searchBody);
}

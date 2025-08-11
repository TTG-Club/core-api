package club.ttg.dnd5.domain.magic.service;

import club.ttg.dnd5.domain.common.rest.dto.PageResponse;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemDetailResponse;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemRequest;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemShortResponse;

public interface MagicItemService {
    boolean existsByUrl(String url);

    MagicItemDetailResponse getItem(String url);

    PageResponse<MagicItemShortResponse> getItems(String searchLine, final int page, final int limit, final String sort, final SearchBody searchBody);

    String addItem(MagicItemRequest itemDto);

    String delete(String itemUrl);

    String updateItem(String url, MagicItemRequest itemDto);

    MagicItemRequest findFormByUrl(String url);
}

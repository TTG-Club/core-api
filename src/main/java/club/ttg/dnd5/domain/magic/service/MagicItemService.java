package club.ttg.dnd5.domain.magic.service;

import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemDetailResponse;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemRequest;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemShortResponse;

import java.util.Collection;

public interface MagicItemService {
    boolean existsByUrl(String url);

    MagicItemDetailResponse getItem(String url);

    Collection<MagicItemShortResponse> getItems(String searchLine, final SearchBody searchBody);

    MagicItem addMagicItem(MagicItemRequest itemDto);

    String deleteMagicItem(String itemUrl);

    String updateMagicItem(String url, MagicItemRequest itemDto);

    MagicItemRequest findFormByUrl(String url);

    MagicItemDetailResponse preview(MagicItemRequest request);
}

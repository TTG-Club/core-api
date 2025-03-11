package club.ttg.dnd5.domain.magic.service;

import club.ttg.dnd5.domain.magic.rest.dto.MagicItemDetailResponse;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemRequest;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemShortResponse;

import java.util.Collection;

public interface MagicItemService {
    boolean existsByUrl(String url);

    MagicItemDetailResponse getItem(String url);

    Collection<MagicItemShortResponse> getItems();

    String addItem(MagicItemRequest itemDto);

    String delete(String itemUrl);

    String updateItem(String url, MagicItemRequest itemDto);
}

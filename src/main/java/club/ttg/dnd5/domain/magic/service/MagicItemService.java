package club.ttg.dnd5.domain.magic.service;

import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemDetailResponse;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemRequest;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemShortResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.Collection;

public interface MagicItemService {
    boolean existsByUrl(String url);

    MagicItemDetailResponse getItem(String url);

    Collection<MagicItemShortResponse> getItems(String searchLine, final SearchBody searchBody);

    String addItem(MagicItemRequest itemDto);

    String delete(String itemUrl);

    String updateItem(String url, MagicItemRequest itemDto);

    MagicItemRequest findFormByUrl(String url);

    MagicItemDetailResponse preview(MagicItemRequest request);

    Collection<MagicItemShortResponse> getItems(@Valid @Size(min = 2) String searchLine, String searchBody);
}

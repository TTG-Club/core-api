package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.background.rest.dto.BackgroundDetailResponse;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundRequest;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundShortResponse;
import club.ttg.dnd5.domain.common.rest.dto.PageResponse;
import club.ttg.dnd5.domain.filter.model.SearchBody;

public interface BackgroundService {
    BackgroundDetailResponse getBackground(String backgroundUrl);

    PageResponse<BackgroundShortResponse> getBackgrounds(String searchLine, final int page, final int limit, final String[] sort, final SearchBody searchBody);

    String addBackground(BackgroundRequest backgroundDto);

    String updateBackgrounds(String backgroundUrl, BackgroundRequest backgroundDto);

    String deleteBackgrounds(String backgroundUrl);

    boolean exists(String backgroundUrl);

    BackgroundRequest findFormByUrl(String url);

    BackgroundDetailResponse preview(BackgroundRequest request);
}

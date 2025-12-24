package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.background.rest.dto.BackgroundDetailResponse;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundRequest;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundShortResponse;
import club.ttg.dnd5.domain.filter.model.SearchBody;

import java.util.Collection;

public interface BackgroundService {
    BackgroundDetailResponse getBackground(String backgroundUrl);

    Collection<BackgroundShortResponse> getBackgrounds(String searchLine, final SearchBody searchBody);

    String addBackground(BackgroundRequest backgroundDto);

    String updateBackgrounds(String backgroundUrl, BackgroundRequest backgroundDto);

    String deleteBackgrounds(String backgroundUrl);

    boolean exists(String backgroundUrl);

    BackgroundRequest findFormByUrl(String url);

    BackgroundDetailResponse preview(BackgroundRequest request);

    Collection<BackgroundRequest> getBackgroundsRaw();
}

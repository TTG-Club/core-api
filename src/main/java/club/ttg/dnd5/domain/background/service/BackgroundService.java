package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.background.rest.dto.BackgroundDetailResponse;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundRequest;
import club.ttg.dnd5.domain.common.dto.ShortResponse;

import java.util.Collection;

public interface BackgroundService {
    BackgroundDetailResponse getBackground(String backgroundUrl);

    Collection<ShortResponse> getBackgrounds();

    BackgroundDetailResponse addBackground(BackgroundRequest backgroundDto);

    BackgroundDetailResponse updateBackgrounds(String backgroundUrl, BackgroundRequest backgroundDto);

    ShortResponse deleteBackgrounds(String backgroundUrl);

    boolean exists(String backgroundUrl);
}

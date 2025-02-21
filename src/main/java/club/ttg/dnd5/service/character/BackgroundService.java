package club.ttg.dnd5.service.character;

import club.ttg.dnd5.dto.character.BackgroundDto;

import java.util.Collection;

public interface BackgroundService {
    BackgroundDto getBackground(String backgroundUrl);

    Collection<BackgroundDto> getBackgrounds();

    BackgroundDto addBackground(club.ttg.dnd5.dto.character.BackgroundDto backgroundDto);

    BackgroundDto updateBackgrounds(String backgroundUrl, BackgroundDto backgroundDto);

    BackgroundDto deleteBackgrounds(String backgroundUrl);

    boolean exists(String backgroundUrl);
}

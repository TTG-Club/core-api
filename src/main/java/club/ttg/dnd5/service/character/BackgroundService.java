package club.ttg.dnd5.service.character;

import club.ttg.dnd5.dto.character.BackgroundDto;

public interface BackgroundService {
    BackgroundDto getBackground(String backgroundUrl);
}

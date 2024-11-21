package club.ttg.dnd5.service.character;

import club.ttg.dnd5.dto.character.BackgroundDto;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.model.character.Background;
import club.ttg.dnd5.repository.character.BackgroundRepository;
import club.ttg.dnd5.utills.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class BackgroundServiceImpl implements BackgroundService {
    private final BackgroundRepository backgroundRepository;

    @Override
    public BackgroundDto getBackground(final String backgroundUrl) {
        return toDTO(findByUrl(backgroundUrl));
    }

    @Override
    public Collection<BackgroundDto> getBackgrounds() {
        return backgroundRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    public BackgroundDto addBackgrounds(final BackgroundDto backgroundDto) {
        return List.of();
    }

    @Override
    public BackgroundDto updateBackgrounds(final String backgroundUrl, final BackgroundDto backgroundDto) {
        return null;
    }

    @Override
    public BackgroundDto deleteBackgrounds(final String backgroundUrl) {
        return null;
    }


    private BackgroundDto toDTO(Background entity) {
        return toDTO(entity, false);
    }

    private BackgroundDto toDTO(Background entity, boolean hideDetails) {
        var dto = new BackgroundDto();
        if (hideDetails) {
            Converter.MAP_ENTITY_TO_BASE_DTO_WITH_HIDE_DETAILS.apply(dto, entity);
        } else {
            Converter.MAP_ENTITY_TO_BASE_DTO.apply(dto, entity);
            Converter.MAP_ENTITY_SOURCE_TO_DTO_SOURCE.apply(dto.getSourceDTO(), entity);
        }
        return dto;
    }

    private Background toEntity(BackgroundDto dto) {
        return toEntity(new Background(), dto);
    }

    private Background toEntity(Background entity, BackgroundDto dto) {
        entity.setUrl(dto.getUrl());
        Converter.MAP_BASE_DTO_TO_ENTITY_NAME.apply(dto, entity);
        Converter.MAP_DTO_SOURCE_TO_ENTITY_SOURCE.apply(dto.getSourceDTO(), entity);
        return entity;
    }

    private Background findByUrl(String url) {
        return backgroundRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found with URL: " + url));
    }
}

package club.ttg.dnd5.service.character;

import club.ttg.dnd5.dto.character.BackgroundDto;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.model.character.Background;
import club.ttg.dnd5.repository.character.BackgroundRepository;
import club.ttg.dnd5.repository.character.FeatRepository;
import club.ttg.dnd5.utills.Converter;
import club.ttg.dnd5.utills.character.BackgroundConverter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class BackgroundServiceImpl implements BackgroundService {
    private final BackgroundRepository backgroundRepository;
    private final FeatRepository featRepository;

    @Override
    public BackgroundDto getBackground(final String backgroundUrl) {
        return toDTO(findByUrl(backgroundUrl));
    }

    @Override
    public Collection<BackgroundDto> getBackgrounds() {
        return backgroundRepository.findAll().stream().map(b -> toDTO(b ,true)).toList();
    }

    @Transactional
    @Override
    public BackgroundDto addBackground(final BackgroundDto backgroundDto) {
        if (backgroundRepository.existsById(backgroundDto.getUrl())) {
            throw new EntityExistException();
        }
        return toDTO(backgroundRepository.save(toEntity(backgroundDto)));
    }

    @Transactional
    @Override
    public BackgroundDto updateBackgrounds(final String url, final BackgroundDto dto) {
        var entity = findByUrl(url);
        if (!url.equals(dto.getUrl())) {
            backgroundRepository.deleteById(url);
        }
        Converter.MAP_BASE_DTO_TO_ENTITY_NAME.apply(dto, entity);
        BackgroundConverter.MAP_DTO_TO_ENTITY.apply(dto, entity);
        Converter.MAP_DTO_SOURCE_TO_ENTITY_SOURCE.apply(dto.getSourceDTO(), entity);
        return toDTO(backgroundRepository.save(toEntity(dto)));
    }

    @Transactional
    @Override
    public BackgroundDto deleteBackgrounds(final String url) {
        var entity = findByUrl(url);
        entity.setHiddenEntity(true);
        return toDTO(backgroundRepository.save(entity));
    }

    @Override
    public boolean existByUrl(final String backgroundUrl) {
        return backgroundRepository.existsById(backgroundUrl);
    }


    private BackgroundDto toDTO(Background entity) {
        return toDTO(entity, false);
    }

    private BackgroundDto toDTO(Background entity, boolean hideDetails) {
        var dto = new BackgroundDto();
        if (hideDetails) {
            Converter.MAP_ENTITY_TO_BASE_DTO_WITH_HIDE_DETAILS.apply(dto, entity);
            Converter.MAP_ENTITY_SOURCE_TO_DTO_SOURCE.apply(dto.getSourceDTO(), entity);
        } else {
            Converter.MAP_ENTITY_TO_BASE_DTO.apply(dto, entity);
            BackgroundConverter.MAP_ENTITY_TO_DTO_.apply(dto, entity);
            dto.setFeat(entity.getFeat().getName());
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
        BackgroundConverter.MAP_DTO_TO_ENTITY.apply(dto, entity);
        var feat = featRepository.findByName(dto.getFeat()).orElseThrow(EntityExistException::new);
        entity.setFeat(feat);
        return entity;
    }

    private Background findByUrl(String url) {
        return backgroundRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found with URL: " + url));
    }
}

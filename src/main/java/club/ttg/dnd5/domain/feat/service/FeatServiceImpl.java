package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.utills.Converter;
import club.ttg.dnd5.utills.character.FeatConverter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class FeatServiceImpl implements FeatService {
    private final FeatRepository featRepository;

    @Override
    public FeatDetailResponse getFeat(final String featUrl) {
        return toDTO(findByUrl(featUrl));
    }

    @Override
    public Collection<FeatDetailResponse> getFeats() {
        return featRepository.findAll()
                .stream()
                .map(f -> toDTO(f, true))
                .toList();
    }

    @Transactional
    @Override
    public FeatDetailResponse addFeat(final FeatDetailResponse dto) {
        if (featRepository.existsById(dto.getUrl())) {
            throw new EntityExistException("Feature exist");
        }
        var entity = new Feat();
        Converter.MAP_BASE_DTO_TO_ENTITY_NAME.apply(dto, entity);
        FeatConverter.MAP_DTO_TO_ENTITY.apply(dto, entity);
        Converter.MAP_DTO_SOURCE_TO_ENTITY_SOURCE.apply(dto.getSourceDTO(), entity);
        return toDTO(entity);
    }

    @Transactional
    @Override
    public FeatDetailResponse updateFeat(final String featUrl, final FeatDetailResponse dto) {
        var entity = findByUrl(featUrl);
        if (!featUrl.equalsIgnoreCase(dto.getUrl())) {
            featRepository.deleteById(featUrl);
        }
        Converter.MAP_BASE_DTO_TO_ENTITY_NAME.apply(dto, entity);
        FeatConverter.MAP_DTO_TO_ENTITY.apply(dto, entity);
        Converter.MAP_DTO_SOURCE_TO_ENTITY_SOURCE.apply(dto.getSourceDTO(), entity);
        return toDTO(featRepository.save(entity));
    }

    @Transactional
    @Override
    public FeatDetailResponse delete(final String featUrl) {
        var entity = findByUrl(featUrl);
        entity.setHiddenEntity(true);
        return toDTO(featRepository.save(entity));
    }

    @Override
    public boolean exists(final String featUrl) {
        return featRepository.existsById(featUrl);
    }

    private FeatDetailResponse toDTO(Feat feat) {
        return toDTO(feat, false);
    }

    private FeatDetailResponse toDTO(Feat feat, boolean hideDetails) {
        FeatDetailResponse dto = new FeatDetailResponse();
        if (hideDetails) {
            Converter.MAP_ENTITY_TO_BASE_DTO_WITH_HIDE_DETAILS.apply(dto, feat);
        } else {
            FeatConverter.MAP_ENTITY_TO_DTO_.apply(dto, feat);
            Converter.MAP_ENTITY_TO_BASE_DTO.apply(dto, feat);
        }
        return dto;
    }

    private Feat findByUrl(String url) {
        return featRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Class not found with URL: " + url));
    }
}

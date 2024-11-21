package club.ttg.dnd5.service.character;

import club.ttg.dnd5.dto.character.ClassDto;
import club.ttg.dnd5.dto.character.FeatDto;
import club.ttg.dnd5.model.character.ClassCharacter;
import club.ttg.dnd5.model.character.ClassFeature;
import club.ttg.dnd5.model.character.Feat;
import club.ttg.dnd5.repository.character.FeatRepository;
import club.ttg.dnd5.utills.Converter;
import club.ttg.dnd5.utills.character.ClassConverter;
import club.ttg.dnd5.utills.character.ClassFeatureConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FeatServiceImpl implements FeatService {
    private final FeatRepository featRepository;

    @Override
    public FeatDto getFeat(final String featUrl) {
        return null;
    }

    @Override
    public Collection<FeatDto> getFeats() {
        return List.of();
    }

    @Override
    public FeatDto addFeat(final FeatDto featDto) {
        return null;
    }

    @Override
    public FeatDto updateFeat(final FeatDto featDto) {
        return null;
    }

    @Override
    public FeatDto delete(final String featUrl) {
        return null;
    }
    private FeatDto toDTO(Feat feat) {
        return toDTO(feat, false);
    }

    private FeatDto toDTO(Feat feat, boolean hideDetails) {
        FeatDto dto = new FeatDto();
        if (hideDetails) {
            Converter.MAP_ENTITY_TO_BASE_DTO_WITH_HIDE_DETAILS.apply(dto, feat);
        } else {
            ClassConverter.MAP_ENTITY_TO_DTO_.apply(dto, feat);
            Converter.MAP_ENTITY_TO_BASE_DTO.apply(dto, feat);
            Converter.MAP_ENTITY_SOURCE_TO_DTO_SOURCE.apply(dto.getSourceDTO(), feat);
        }
        return dto;
    }
}

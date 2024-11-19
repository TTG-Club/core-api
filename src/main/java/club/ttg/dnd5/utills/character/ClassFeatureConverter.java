package club.ttg.dnd5.utills.character;

import club.ttg.dnd5.dto.character.ClassFeatureDto;
import club.ttg.dnd5.model.character.ClassFeature;
import club.ttg.dnd5.utills.Converter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.function.BiFunction;

import static club.ttg.dnd5.utills.Converter.MAP_ENTITY_TO_DTO_WITH_LEVEL;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassFeatureConverter {

    // Converter functions
    private static final BiFunction<ClassFeatureDto, ClassFeature, ClassFeature> DTO_TO_ENTITY_CONVERTER =
            (response, feature) -> {
                Converter.MAP_BASE_DTO_TO_ENTITY_NAME.apply(response, feature);
                Converter.MAP_DTO_SOURCE_TO_ENTITY_SOURCE.apply(response, feature);
                feature.setTags(response.getTags());
                feature.setDescription(response.getDescription());
                return feature;
            };

    private static final BiFunction<ClassFeature, ClassFeatureDto, ClassFeatureDto> ENTITY_TO_DTO_CONVERTER =
            (feature, dto) -> {
                Converter.MAP_ENTITY_TO_BASE_DTO.apply(dto, feature);
                Converter.MAP_ENTITY_SOURCE_TO_DTO_SOURCE.apply(dto, feature);
                dto.setTags(feature.getTags());
                dto.setDescription(feature.getFeatureDescription());
                return dto;
            };

    public static ClassFeature toEntityFeature(ClassFeatureDto response) {
        ClassFeature feature = new ClassFeature();
        MAP_ENTITY_TO_DTO_WITH_LEVEL.apply(response, feature);
        return DTO_TO_ENTITY_CONVERTER.apply(response, feature);
    }

    public static ClassFeatureDto toDtoFeature(ClassFeature feature) {
        var dto = new ClassFeatureDto();
        return ENTITY_TO_DTO_CONVERTER.apply(feature, dto);
    }

    public static Collection<ClassFeatureDto> convertEntityFeatureIntoDTOFeature(Collection<ClassFeature> entityFeatures) {
        return entityFeatures.stream()
                .map(ClassFeatureConverter::toDtoFeature)
                .toList();
    }
}


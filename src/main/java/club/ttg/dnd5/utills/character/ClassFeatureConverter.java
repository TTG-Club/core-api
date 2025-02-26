package club.ttg.dnd5.utills.character;

import club.ttg.dnd5.domain.clazz.rest.dto.ClassFeatureDto;
import club.ttg.dnd5.domain.common.model.Tag;
import club.ttg.dnd5.domain.clazz.model.ClassFeature;
import club.ttg.dnd5.utills.Converter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static club.ttg.dnd5.utills.Converter.MAP_ENTITY_TO_DTO_WITH_LEVEL;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassFeatureConverter {

    // Converter functions
    private static final BiFunction<ClassFeatureDto, ClassFeature, ClassFeature> DTO_TO_ENTITY_CONVERTER =
            (dto, feature) -> {
                Converter.MAP_BASE_DTO_TO_ENTITY_NAME.apply(dto, feature);
                Converter.MAP_DTO_SOURCE_TO_ENTITY_SOURCE.apply(dto.getSourceDTO(), feature);

                // Convert DTO tags (Set<String>) to entity tags (Set<Tag>)
                Set<Tag> tags = dto.getTags().stream()
                        .map(Tag::new)
                        .collect(Collectors.toSet());
                feature.setTags(tags);

                feature.setDescription(dto.getDescription());
                return feature;
            };

    private static final BiFunction<ClassFeature, ClassFeatureDto, ClassFeatureDto> ENTITY_TO_DTO_CONVERTER =
            (feature, dto) -> {
                Converter.MAP_ENTITY_TO_BASE_DTO.apply(dto, feature);
                Converter.MAP_ENTITY_SOURCE_TO_DTO_SOURCE.apply(dto.getSourceDTO(), feature);

                // Convert entity tags (Set<Tag>) to DTO tags (Set<String>)
                Set<String> tags = feature.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toSet());
                dto.setTags(tags);

                dto.setDescription(feature.getDescription());
                return dto;
            };

    public static ClassFeatureDto toDtoFeature(ClassFeature feature) {
        var dto = new ClassFeatureDto();
        return ENTITY_TO_DTO_CONVERTER.apply(feature, dto);
    }

    public static ClassFeature toEntityFeature(ClassFeatureDto response) {
        ClassFeature feature = new ClassFeature();
        feature.setLevel(response.getLevel());
        MAP_ENTITY_TO_DTO_WITH_LEVEL.apply(response, feature);
        return DTO_TO_ENTITY_CONVERTER.apply(response, feature);
    }

    public static Collection<ClassFeatureDto> convertEntityFeatureIntoDTOFeature(Collection<ClassFeature> entityFeatures) {
        return entityFeatures.stream()
                .map(ClassFeatureConverter::toDtoFeature)
                .toList();
    }
}


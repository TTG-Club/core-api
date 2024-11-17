package club.ttg.dnd5.utills.species;

import club.ttg.dnd5.dto.base.TagDto;
import club.ttg.dnd5.dto.species.SpeciesFeatureDto;
import club.ttg.dnd5.model.species.Species;
import club.ttg.dnd5.model.species.SpeciesFeature;
import club.ttg.dnd5.utills.Converter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpeciesFeatureConverter {
    // Converts SpeciesFeatureDto to SpeciesFeature
    private static final BiFunction<SpeciesFeatureDto, SpeciesFeature, SpeciesFeature> DTO_TO_ENTITY_CONVERTER =
            (response, speciesFeature) -> {
                Converter.MAP_BASE_DTO_TO_ENTITY_NAME.apply(response, speciesFeature);
                Converter.MAP_DTO_SOURCE_TO_ENTITY_SOURCE.apply(response, speciesFeature);

                // Convert tags from TagDto to Map<String, String>
                Map<String, String> tags = response.getTags().stream()
                        .collect(Collectors.toMap(TagDto::getName, TagDto::getValue));
                speciesFeature.setTags(tags);

                speciesFeature.setFeatureDescription(response.getDescription());
                return speciesFeature;
            };

    // Converts SpeciesFeature to SpeciesFeatureDto
    private static final BiFunction<SpeciesFeature, SpeciesFeatureDto, SpeciesFeatureDto> ENTITY_TO_DTO_CONVERTER =
            (feature, dto) -> {
                Converter.MAP_ENTITY_TO_BASE_DTO.apply(dto, feature);
                Converter.MAP_ENTITY_SOURCE_TO_DTO_SOURCE.apply(dto, feature);

                // Convert tags from Map<String, String> to a collection of TagDto
                Collection<TagDto> tags = feature.getTags().entrySet().stream()
                        .map(entry -> new TagDto(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
                dto.setTags(tags);

                dto.setDescription(feature.getFeatureDescription());
                return dto;
            };

    // Converts SpeciesFeatureDto to SpeciesFeature
    public static SpeciesFeature toEntityFeature(SpeciesFeatureDto response) {
        SpeciesFeature speciesFeature = new SpeciesFeature();
        return DTO_TO_ENTITY_CONVERTER.apply(response, speciesFeature);
    }

    // Converts SpeciesFeature to SpeciesFeatureDto
    public static SpeciesFeatureDto toDTOFeature(SpeciesFeature feature) {
        SpeciesFeatureDto dto = new SpeciesFeatureDto();
        return ENTITY_TO_DTO_CONVERTER.apply(feature, dto);
    }

    // Converts a collection of SpeciesFeatureDto to SpeciesFeature and sets to species
    public static void convertDTOFeatureIntoEntityFeature(Collection<SpeciesFeatureDto> dtoFeatures, Species species) {
        Collection<SpeciesFeature> features = dtoFeatures.stream()
                .map(SpeciesFeatureConverter::toEntityFeature)
                .toList();
        species.setFeatures(features);
    }

    // Converts a collection of SpeciesFeature to SpeciesFeatureDto
    public static Collection<SpeciesFeatureDto> convertEntityFeatureIntoDTOFeature(Collection<SpeciesFeature> entityFeatures) {
        return entityFeatures.stream()
                .map(SpeciesFeatureConverter::toDTOFeature)
                .toList();
    }
}


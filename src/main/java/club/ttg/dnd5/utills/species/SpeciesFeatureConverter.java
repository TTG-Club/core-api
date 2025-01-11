package club.ttg.dnd5.utills.species;

import club.ttg.dnd5.dto.species.SpeciesFeatureDto;
import club.ttg.dnd5.model.base.Tag;
import club.ttg.dnd5.model.species.Species;
import club.ttg.dnd5.model.species.SpeciesFeature;
import club.ttg.dnd5.utills.Converter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpeciesFeatureConverter {
    // Converts SpeciesFeatureDto to SpeciesFeature
    private static final BiFunction<SpeciesFeatureDto, SpeciesFeature, SpeciesFeature> DTO_TO_ENTITY_CONVERTER =
            (response, speciesFeature) -> {
                if (response == null || speciesFeature == null) {
                    return null;
                }

                if (response.getSourceDTO() != null) {
                    Converter.MAP_DTO_SOURCE_TO_ENTITY_SOURCE.apply(response.getSourceDTO(), speciesFeature);
                }

                if (response.getTags() != null) {
                    Set<Tag> tags = response.getTags().stream()
                            .map(Tag::new)
                            .collect(Collectors.toSet());
                    speciesFeature.setTags(tags);
                }

                if (response.getDescription() != null) {
                    speciesFeature.setFeatureDescription(response.getDescription());
                }
                Converter.MAP_BASE_DTO_TO_ENTITY_NAME.apply(response, speciesFeature);

                return speciesFeature;
            };

    // Converts SpeciesFeature to SpeciesFeatureDto
    // Converts SpeciesFeature to SpeciesFeatureDto
    private static final BiFunction<SpeciesFeature, SpeciesFeatureDto, SpeciesFeatureDto> ENTITY_TO_DTO_CONVERTER =
            (feature, dto) -> {
                if (feature == null || dto == null) {
                    return null; // Or throw an exception if you prefer
                }

                // Ensure sourceDTO is not null before mapping
                if (dto.getSourceDTO() != null) {
                    Converter.MAP_ENTITY_SOURCE_TO_DTO_SOURCE.apply(dto.getSourceDTO(), feature);
                }

                // Convert entity tags (Set<Tag>) to DTO tags (Set<String>)
                if (feature.getTags() != null) {
                    Set<String> tags = feature.getTags().stream()
                            .map(Tag::getName)
                            .collect(Collectors.toSet());
                    dto.setTags(tags);
                }

                // Ensure description is not null before setting
                if (feature.getFeatureDescription() != null) {
                    dto.setDescription(feature.getFeatureDescription());
                }

                // Apply name mapping (assuming this method also handles null safety)
                Converter.MAP_ENTITY_TO_BASE_DTO.apply(dto, feature);

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

package club.ttg.dnd5.utills.species;

import club.ttg.dnd5.dto.species.SpeciesFeatureResponse;
import club.ttg.dnd5.model.species.Species;
import club.ttg.dnd5.model.species.SpeciesFeature;
import club.ttg.dnd5.utills.Converter;

import java.util.Collection;

public class SpeciesFeatureConverter {
    private SpeciesFeatureConverter() {

    }

    public static SpeciesFeature toEntityFeature(SpeciesFeatureResponse response) {
        SpeciesFeature speciesFeature = new SpeciesFeature();
        Converter.mapBaseDTOToEntityName(response, speciesFeature);
        Converter.mapDTOSourceToEntitySource(response, speciesFeature);
        speciesFeature.setTags(response.getTags());
        speciesFeature.setFeatureDescription(response.getDescription());
        return speciesFeature;
    }

    public static SpeciesFeatureResponse toDTOFeature(SpeciesFeature feature) {
        SpeciesFeatureResponse dto = new SpeciesFeatureResponse();
        Converter.mapEntityToBaseDTO(dto, feature);
        Converter.mapEntitySourceToDTOSource(dto, feature);
        dto.setTags(feature.getTags());
        dto.setDescription(feature.getFeatureDescription());
        return dto;
    }

    public static void convertDTOFeatureIntoEntityFeature(Collection<SpeciesFeatureResponse> dtoFeatures, Species species) {
        Collection<SpeciesFeature> features = dtoFeatures.stream()
                .map(SpeciesFeatureConverter::toEntityFeature)
                .toList();
        species.setFeatures(features);
    }

    public static Collection<SpeciesFeatureResponse> convertEntityFeatureIntoDTOFeature(Collection<SpeciesFeature> entityFeatures) {
        return entityFeatures.stream()
                .map(SpeciesFeatureConverter::toDTOFeature)
                .toList();
    }
}

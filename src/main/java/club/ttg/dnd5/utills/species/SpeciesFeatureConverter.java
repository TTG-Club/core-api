package club.ttg.dnd5.utills.species;

import club.ttg.dnd5.dto.EntryDto;
import club.ttg.dnd5.dto.species.SpeciesFeatureResponse;
import club.ttg.dnd5.model.species.Species;
import club.ttg.dnd5.model.species.SpeciesFeature;
import club.ttg.dnd5.utills.Converter;

import java.util.Collection;
import java.util.Collections;

public class SpeciesFeatureConverter {
    private SpeciesFeatureConverter() {

    }

    public static SpeciesFeature toEntityFeature(SpeciesFeatureResponse response) {
        SpeciesFeature speciesFeature = new SpeciesFeature();
        Converter.mapBaseDTOToEntityName(response, speciesFeature);
        speciesFeature.setTags(response.getTags());
        return speciesFeature;
    }

    public static SpeciesFeatureResponse toDTOFeature(SpeciesFeature feature) {
        SpeciesFeatureResponse dto = new SpeciesFeatureResponse();
        dto.setUrl(feature.getUrl());
        EntryDto entries = new EntryDto();
        entries.setName(feature.getName());
        entries.setEntries(Collections.singletonList(feature.getEntries()));
        return dto;
    }

    //TODO saving in the database
    public static void convertDTOFeatureIntoEntityFeature(Collection<SpeciesFeatureResponse> dtoFeatures, Species species) {
        Collection<SpeciesFeature> features = dtoFeatures.stream()
                .map(SpeciesFeatureConverter::toEntityFeature)
                .toList();
        species.setFeatures(features);
    }
}

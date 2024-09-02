package club.ttg.dnd5.mapper.species;

import club.ttg.dnd5.dto.base.NameBasedDTO;
import club.ttg.dnd5.dto.base.SourceResponse;
import club.ttg.dnd5.dto.species.SpeciesResponse;
import club.ttg.dnd5.model.Source;
import club.ttg.dnd5.model.species.Species;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

public interface SpeciesMapper {
    SpeciesMapper INSTANCE = Mappers.getMapper(SpeciesMapper.class);

    @Mapping(source = "parent", target = "parent")
    @Mapping(source = "subSpecies", target = "subSpecies")
    @Mapping(source = "species", target = "source", qualifiedByName = "toSpeciesSourceDTO")
    SpeciesResponse toDTO(Species species);

    @Mapping(source = "parent", target = "parent")
    @Mapping(source = "subSpecies", target = "subSpecies")
    @Mapping(source = "speciesResponse", target = "source", qualifiedByName = "toSpeciesSourceEntity")
    Species toEntity(SpeciesResponse speciesResponse);

    default List<SpeciesResponse> convertNotDetailList(List<Species> speciesList) {
        List<SpeciesResponse> speciesResponses = new ArrayList<>();
        for (Species species : speciesList) {
            SpeciesResponse speciesResponse = new SpeciesResponse();
            speciesResponse.setDetail(false);
            speciesResponse.setNameBasedDTO(new NameBasedDTO(
                    species.getName(),
                    species.getEnglish(),
                    species.getAlternative(),
                    species.getDescription()
            ));
            speciesResponse.setSource(SourceMapper.INSTANCE.toSourceResponse(species.getSource()));
            speciesResponse.setUrl(species.getUrl());
            speciesResponses.add(speciesResponse);
        }
        return speciesResponses;
    }

    @Named("toSpeciesSourceDTO")
    default SourceResponse toSpeciesSourceDTO(Species species) {
        SourceResponse sourceResponse = new SourceResponse();
        sourceResponse.setSource(species.getSource().getSource());
        sourceResponse.setPage(species.getPage());
        return sourceResponse;
    }

    @Named("toSpeciesSourceEntity")
    default Source toSpeciesSourceEntity(SpeciesResponse speciesResponse) {
        Source source = new Source();
        source.setSource(speciesResponse.getSource());
        return source;
    }
}
package club.ttg.dnd5.mapper.species;

import club.ttg.dnd5.dto.species.SpeciesResponse;
import club.ttg.dnd5.model.species.Species;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface SpeciesMapper {
    SpeciesMapper INSTANCE = Mappers.getMapper(SpeciesMapper.class);
    SpeciesResponse toDTO(Species species);
    Species toEntity(SpeciesResponse speciesResponse);

    default List<SpeciesResponse> convertNotDetailList(List<Species> speciesList) {
        List<SpeciesResponse> speciesResponses = new ArrayList<>();
        for (Species species : speciesList) {
            SpeciesResponse speciesResponse = new SpeciesResponse();
            speciesResponse.setDetail(false);
            speciesResponse.setName(species.getName());
            speciesResponse.setEnglish(species.getEnglish());
            speciesResponse.setUrl(species.getUrl());
            speciesResponses.add(speciesResponse);
        }
        return speciesResponses;
    }
}


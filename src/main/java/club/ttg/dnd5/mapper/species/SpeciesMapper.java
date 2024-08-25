package club.ttg.dnd5.mapper.species;

import club.ttg.dnd5.dto.species.SpeciesResponse;
import club.ttg.dnd5.model.species.Species;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SpeciesMapper {
    SpeciesMapper INSTANCE = Mappers.getMapper(SpeciesMapper.class);
    SpeciesResponse toEntity(Species species);
    Species toDTO(SpeciesResponse speciesResponse);
}


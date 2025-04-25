package club.ttg.dnd5.domain.species.rest.mapper;

import club.ttg.dnd5.domain.species.model.CreatureProperties;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesPropertiesRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@Mapper(componentModel = "spring")
public interface CreaturePropertiesMapper {

    @Mapping(source = "type", target = "type")
    @Mapping(source = "speed", target = "movementAttributes.base")
    @Mapping(source = "fly", target = "movementAttributes.fly")
    @Mapping(source = "climb", target = "movementAttributes.climb")
    @Mapping(source = "swim", target = "movementAttributes.swim")
    SpeciesPropertiesRequest toSpeciesPropertiesRequest(CreatureProperties properties);
}

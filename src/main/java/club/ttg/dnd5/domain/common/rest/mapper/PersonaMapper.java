package club.ttg.dnd5.domain.common.rest.mapper;

import club.ttg.dnd5.domain.common.model.notification.Persona;
import club.ttg.dnd5.domain.common.rest.dto.notification.PersonaRequest;
import club.ttg.dnd5.domain.common.rest.dto.notification.PersonaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PersonaMapper {
    PersonaResponse toResponse(Persona persona);

    Persona toEntity(PersonaRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(PersonaRequest request, @MappingTarget Persona entity);
}

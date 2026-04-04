package club.ttg.dnd5.domain.common.rest.mapper;

import club.ttg.dnd5.domain.common.model.notification.Persona;
import club.ttg.dnd5.domain.common.rest.dto.notification.PersonaRequest;
import club.ttg.dnd5.domain.common.rest.dto.notification.PersonaResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface PersonaMapper {
    PersonaResponse toResponse(Persona persona);

    Persona toEntity(PersonaRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(PersonaRequest request, @MappingTarget Persona entity);
}

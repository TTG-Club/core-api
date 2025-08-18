package club.ttg.dnd5.domain.workshop.rest.mapper;

import club.ttg.dnd5.domain.workshop.rest.dto.WorkshopResponse;
import club.ttg.dnd5.domain.workshop.rest.dto.WorkshopPairDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkshopMapper {

    @Mapping(source = "sectionType", target = "type")
    @Mapping(source = "count", target = "counters.created")
    WorkshopResponse toResponse(WorkshopPairDto workshopPairDto);
}

package club.ttg.dnd5.domain.roadmap.rest.mapper;

import club.ttg.dnd5.domain.roadmap.model.Roadmap;
import club.ttg.dnd5.domain.roadmap.rest.dto.RoadmapResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoadmapMapper {
    @Mapping(target = "rate", ignore = true)
    RoadmapResponse toResponse(Roadmap roadmap);
    void update(@MappingTarget Roadmap target, Roadmap source);
}

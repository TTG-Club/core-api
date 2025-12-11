package club.ttg.dnd5.domain.roadmap.rest.mapper;

import club.ttg.dnd5.domain.roadmap.model.Roadmap;
import club.ttg.dnd5.domain.roadmap.rest.dto.RoadmapRequest;
import club.ttg.dnd5.domain.roadmap.rest.dto.RoadmapResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoadmapMapper {
    @Mapping(target = "rate", ignore = true)
    RoadmapResponse toResponse(Roadmap roadmap);

    Roadmap toEntity(RoadmapRequest roadmapRequest);

    void update(@MappingTarget Roadmap target, RoadmapRequest source);

    RoadmapRequest toRequest(Roadmap roadmap);
}

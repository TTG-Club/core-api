package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.action.CreatureAction;
import club.ttg.dnd5.domain.beastiary.rest.dto.ActionRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.ActionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ActionMapper {
    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "name.english", target = "english")
    CreatureAction toEntity(ActionRequest request);

    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "name.english", target = "english")
    CreatureAction toEntity(ActionResponse response);

    // Entity → Response DTO
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    ActionResponse toResponse(CreatureAction action);

    // Entity → Request DTO (если нужно)
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    ActionRequest toRequest(CreatureAction action);
}
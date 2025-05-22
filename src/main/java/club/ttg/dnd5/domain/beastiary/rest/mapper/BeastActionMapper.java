package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.action.BeastAction;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastActionRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastActionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BeastActionMapper {
    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "name.english", target = "english")
    BeastAction toEntity(BeastActionRequest request);

    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "name.english", target = "english")
    BeastAction toEntity(BeastActionResponse response);

    // Entity → Response DTO
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    BeastActionResponse toResponse(BeastAction action);

    // Entity → Request DTO (если нужно)
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    BeastActionRequest toRequest(BeastAction action);
}
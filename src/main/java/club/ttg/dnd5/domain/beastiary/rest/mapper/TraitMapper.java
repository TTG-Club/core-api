package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.CreatureTrait;
import club.ttg.dnd5.domain.beastiary.rest.dto.TraitRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.TraitResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TraitMapper {
    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "name.english", target = "english")
    CreatureTrait toEntity(TraitRequest request);

    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "name.english", target = "english")
    CreatureTrait toEntity(TraitResponse response);

    // Entity → Response DTO
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    TraitResponse toResponse(CreatureTrait trait);

    // Entity → Request DTO (если нужно)
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    TraitRequest toRequest(CreatureTrait trait);
}

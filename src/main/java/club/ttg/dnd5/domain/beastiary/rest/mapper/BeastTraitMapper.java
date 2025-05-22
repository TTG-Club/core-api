package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.BeastTrait;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastTraitRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastTraitResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BeastTraitMapper {
    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "name.english", target = "english")
    BeastTrait toEntity(BeastTraitRequest request);

    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "name.english", target = "english")
    BeastTrait toEntity(BeastTraitResponse response);

    // Entity → Response DTO
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    BeastTraitResponse toResponse(BeastTrait trait);

    // Entity → Request DTO (если нужно)
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    BeastTraitRequest toRequest(BeastTrait trait);
}

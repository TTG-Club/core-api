package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.action.BeastAction;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastActionResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastTraitResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BeastTraitMapper {
    BeastAction toEntity(BeastTraitResponse response);
    BeastTraitResponse toResponse(BeastAction action);

}

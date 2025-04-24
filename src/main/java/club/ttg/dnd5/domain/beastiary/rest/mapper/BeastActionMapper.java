package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.action.BeastAction;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastActionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BeastActionMapper {
    BeastAction toEntity(BeastActionResponse response);
    BeastActionResponse toResponse(BeastAction action);

}

package club.ttg.dnd5.domain.moderation.rest.mapper;

import club.ttg.dnd5.domain.moderation.model.ModerationEntity;
import club.ttg.dnd5.domain.moderation.rest.dto.ModerationResponse;
import club.ttg.dnd5.domain.moderation.rest.dto.ModerationShortResponse;
import club.ttg.dnd5.domain.common.model.Timestamped;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ModerationMapper {

    ModerationResponse toResponse(ModerationEntity moderationEntity);

    ModerationShortResponse toShortResponse(ModerationEntity moderationEntity);

    ModerationEntity toEntity(Timestamped background);
}

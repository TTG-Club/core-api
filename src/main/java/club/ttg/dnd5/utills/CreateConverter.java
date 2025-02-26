package club.ttg.dnd5.utills;

import club.ttg.dnd5.dto.base.create.CreateBaseDTO;
import club.ttg.dnd5.domain.common.model.NamedEntity;

import java.util.function.BiFunction;

public class CreateConverter {
    // Function to map CreateBaseDTO to Entity Name
    public static final BiFunction<CreateBaseDTO, NamedEntity, NamedEntity> MAP_BASE_DTO_TO_ENTITY_NAME = (dto, entity) -> {
        entity.setUrl(dto.getUrl());
        entity.setImageUrl(dto.getImageUrl());
        if (dto.getNameBasedDTO() != null) {
            entity.setName(dto.getNameBasedDTO().getName());
            entity.setEnglish(dto.getNameBasedDTO().getEnglish());
            entity.setAlternative(String.join(",", dto.getNameBasedDTO().getAlternative()));
            entity.setShortName(dto.getNameBasedDTO().getShortName());
        }
        entity.setDescription(dto.getDescription());
        return entity;
    };
}

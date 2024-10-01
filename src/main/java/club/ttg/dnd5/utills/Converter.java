package club.ttg.dnd5.utills;

import club.ttg.dnd5.dto.base.BaseDTO;
import club.ttg.dnd5.dto.base.DetailableDTO;
import club.ttg.dnd5.dto.base.HasSourceDTO;
import club.ttg.dnd5.dto.base.NameBasedDTO;
import club.ttg.dnd5.dto.species.CreaturePropertiesDTO;
import club.ttg.dnd5.model.base.CreatureProperties;
import club.ttg.dnd5.model.base.HasSourceEntity;
import club.ttg.dnd5.model.base.NamedEntity;

public class Converter {
    // D from DTO, E from Entity
    private Converter() {
    }

    public static <D extends BaseDTO, E extends NamedEntity> E mapBaseDTOToEntityName(D dto, E entity) {
        entity.setUrl(dto.getUrl());
        entity.setName(dto.getNameBasedDTO().getName());
        entity.setEnglish(dto.getNameBasedDTO().getEnglish());
        entity.setAlternative(dto.getNameBasedDTO().getAlternative());
        entity.setDescription(dto.getNameBasedDTO().getDescription());
        return entity;
    }

    public static <D extends BaseDTO, E extends NamedEntity> D mapEntityToBaseDTO(D dto, E entity) {
        dto.setUrl(entity.getUrl());
        dto.setNameBasedDTO(new NameBasedDTO());
        dto.getNameBasedDTO().setName(entity.getName());
        dto.getNameBasedDTO().setEnglish(entity.getEnglish());
        dto.getNameBasedDTO().setAlternative(entity.getAlternative());
        dto.getNameBasedDTO().setDescription(entity.getDescription());
        return dto;
    }

    public static <D extends CreaturePropertiesDTO, E extends CreatureProperties> E mapCreaturePropertiesDTOToEntity(D dto, E entity) {
        entity.setSize(dto.getSize());
        entity.setType(dto.getType());
        entity.setSpeed(dto.getSpeed());
        entity.setFly(dto.getFly());
        entity.setClimb(dto.getClimb());
        entity.setSwim(dto.getSwim());
        entity.setDarkVision(dto.getDarkVision());
        return entity;
    }

    public static <D extends CreaturePropertiesDTO, E extends CreatureProperties> D mapEntityToCreaturePropertiesDTO(D dto, E entity) {
        dto.setSize(entity.getSize());
        dto.setType(entity.getType());
        dto.setSpeed(entity.getSpeed());
        dto.setFly(entity.getFly());
        dto.setClimb(entity.getClimb());
        dto.setSwim(entity.getSwim());
        dto.setDarkVision(entity.getDarkVision());
        return dto;
    }

    public static <D extends HasSourceDTO, E extends HasSourceEntity> E mapDTOSourceToEntitySource(D dto, E entity) {
        entity.setPage(dto.getPage());
        entity.setSource(dto.getSource());
        return entity;
    }

    public static <D extends HasSourceDTO, E extends HasSourceEntity> D mapEntitySourceToDTOSource(D dto, E entity) {
        dto.setPage(entity.getPage());
        if (entity.getSource() != null) {
            dto.setSource(entity.getSource().getSource());
        }
        return dto;
    }

    public static <D extends BaseDTO & DetailableDTO, E extends NamedEntity> D mapEntityToBaseDTOWithDetail(D dto, E entity, boolean detail) {
        mapEntityToBaseDTO(dto, entity);
        if (!detail) {
            dto.hideDetails();
        }
        return dto;
    }
}

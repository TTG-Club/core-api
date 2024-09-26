package club.ttg.dnd5.utills;

import club.ttg.dnd5.dto.base.BaseDTO;
import club.ttg.dnd5.dto.base.NameBasedDTO;
import club.ttg.dnd5.dto.species.CreaturePropertiesDTO;
import club.ttg.dnd5.model.base.CreatureProperties;
import club.ttg.dnd5.model.base.NamedEntity;

//TODO change maybe from void to Objects
public class Converter {
    private Converter() {}
    //D from DTO, E from Entity
    public static  <D extends BaseDTO, E extends NamedEntity> void fillEntityNameFromBaseDTO(D dto, E entity) {
        entity.setUrl(dto.getUrl());
        entity.setName(dto.getNameBasedDTO().getName());
        entity.setEnglish(dto.getNameBasedDTO().getEnglish());
        entity.setAlternative(dto.getNameBasedDTO().getAlternative());
        entity.setDescription(dto.getNameBasedDTO().getDescription());
    }

    public static  <D extends BaseDTO, E extends NamedEntity> void fillDTOFromEntity(D dto, E entity) {
        dto.setUrl(entity.getUrl());
        dto.setNameBasedDTO(new NameBasedDTO());
        dto.getNameBasedDTO().setName(entity.getName());
        dto.getNameBasedDTO().setEnglish(entity.getEnglish());
        dto.getNameBasedDTO().setAlternative(entity.getAlternative());
        dto.getNameBasedDTO().setDescription(entity.getDescription());
    }

    public static <D extends CreaturePropertiesDTO, E extends CreatureProperties> void fillEntityCreaturePropertiesFromDTO(D dto, E entity) {
        entity.setSize(dto.getSize());
        entity.setType(dto.getType());
        entity.setSpeed(dto.getSpeed());
        entity.setFly(dto.getFly());
        entity.setClimb(dto.getClimb());
        entity.setSwim(dto.getSwim());
        entity.setDarkVision(dto.getDarkVision());
    }

    public static <D extends CreaturePropertiesDTO, E extends CreatureProperties> void fillDTOCreaturePropertiesFromEntity(D dto, E entity) {
        dto.setSize(entity.getSize());
        dto.setType(entity.getType());
        dto.setSpeed(entity.getSpeed());
        dto.setFly(entity.getFly());
        dto.setClimb(entity.getClimb());
        dto.setSwim(entity.getSwim());
        dto.setDarkVision(entity.getDarkVision());
    }
}

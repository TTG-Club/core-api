package club.ttg.dnd5.utills;

import club.ttg.dnd5.dto.base.BaseDTO;
import club.ttg.dnd5.dto.base.DetailableDTO;
import club.ttg.dnd5.dto.base.HasSourceDTO;
import club.ttg.dnd5.dto.base.NameBasedDTO;
import club.ttg.dnd5.dto.species.CreaturePropertiesDTO;
import club.ttg.dnd5.model.base.CreatureProperties;
import club.ttg.dnd5.model.base.HasSourceEntity;
import club.ttg.dnd5.model.base.NamedEntity;
import club.ttg.dnd5.model.book.Book;
import club.ttg.dnd5.model.book.Source;

public class Converter {
    // D from DTO, E from Entity
    private Converter() {
    }

    public static <D extends BaseDTO, E extends NamedEntity> E mapBaseDTOToEntityName(D dto, E entity) {
        entity.setUrl(dto.getUrl());
        entity.setImageUrl(dto.getImageUrl());
        entity.setName(dto.getNameBasedDTO().getName());
        entity.setEnglish(dto.getNameBasedDTO().getEnglish());
        entity.setAlternative(dto.getNameBasedDTO().getAlternative());
        entity.setDescription(dto.getNameBasedDTO().getDescription());
        return entity;
    }

    public static <D extends BaseDTO, E extends NamedEntity> D mapEntityToBaseDTO(D dto, E entity) {
        dto.setUrl(entity.getUrl());
        dto.setImageUrl(entity.getImageUrl());
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

    //D - dto, E - entity, Id - id (acronym), R - repository
    public static <D extends HasSourceDTO, E extends HasSourceEntity>
    E mapDTOSourceToEntitySource(D dto, E entity) {
        String sourceAcronym = dto.getSource();
        Source source = entity.getSource();
        if (source == null) {
            source = new Source();
        }
        Book book = new Book(sourceAcronym);
        book.setSourceAcronym(sourceAcronym);
        source.setBookInfo(book);
        source.setPage(dto.getPage());
        entity.setSource(source);
        return entity;
    }

    public static <D extends HasSourceDTO, E extends HasSourceEntity> D mapEntitySourceToDTOSource(D dto, E entity) {
        if (entity.getSource() != null) {
            dto.setSource(entity.getSource().getSourceAcronym());
            dto.setPage(entity.getSource().getPage());
        }
        return dto;
    }

    public static <D extends BaseDTO & DetailableDTO, E extends NamedEntity> D mapEntityToBaseDTOWithHideDetails(D dto, E entity) {
        mapEntityToBaseDTO(dto, entity);
        dto.hideDetails();
        return dto;
    }
}

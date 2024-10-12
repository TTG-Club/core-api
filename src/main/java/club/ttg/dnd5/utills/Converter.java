package club.ttg.dnd5.utills;

import club.ttg.dnd5.dto.base.BaseDTO;
import club.ttg.dnd5.dto.base.DetailableDTO;
import club.ttg.dnd5.dto.base.HasSourceDTO;
import club.ttg.dnd5.dto.base.NameBasedDTO;
import club.ttg.dnd5.dto.species.CreaturePropertiesDTO;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.model.base.CreatureProperties;
import club.ttg.dnd5.model.base.HasSourceEntity;
import club.ttg.dnd5.model.base.NamedEntity;
import club.ttg.dnd5.model.book.Book;
import club.ttg.dnd5.model.book.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;

import java.util.Optional;

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
    public static <D extends HasSourceDTO, E extends HasSourceEntity, R extends JpaRepository<Book, String>>
    E mapDTOSourceToEntitySource(D dto, E entity, R jpaRepository) {
        try {
            String sourceAcronym = dto.getSource();
            Optional<?> byId = jpaRepository.findById(sourceAcronym);

            if (byId.isEmpty()) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Source wasn't found: " + sourceAcronym);
            } else {
                Source source = entity.getSource();
                Book book = (Book) byId.get();
                source.setBookInfo(book);
                source.setId(sourceAcronym);
                source.setPage(dto.getPage());
                return entity;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <D extends HasSourceDTO, E extends HasSourceEntity> D mapEntitySourceToDTOSource(D dto, E entity) {
        if (entity.getSource() != null) {
            dto.setSource(entity.getSource().getId());
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

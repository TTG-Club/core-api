package club.ttg.dnd5.utills;


import club.ttg.dnd5.dictionary.Size;
import club.ttg.dnd5.dictionary.beastiary.CreatureType;
import club.ttg.dnd5.dto.base.*;
import club.ttg.dnd5.dto.character.ClassFeatureDto;
import club.ttg.dnd5.dto.species.CreaturePropertiesDto;
import club.ttg.dnd5.dto.species.MovementAttributes;
import club.ttg.dnd5.model.base.CreatureProperties;
import club.ttg.dnd5.model.base.HasSourceEntity;
import club.ttg.dnd5.model.base.NamedEntity;
import club.ttg.dnd5.model.book.Book;
import club.ttg.dnd5.model.book.Source;
import club.ttg.dnd5.model.character.ClassFeature;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Converter {
    public static final BiConsumer<GroupStrategy, Source> STRATEGY_SOURCE_CONSUMER = GroupStrategy::determineGroup;

    // Function to map base DTO to Entity Name
    public static final BiFunction<BaseDTO, NamedEntity, NamedEntity> MAP_BASE_DTO_TO_ENTITY_NAME = (dto, entity) -> {
        entity.setUrl(dto.getUrl());
        entity.setImageUrl(dto.getImageUrl());
        if (dto.getNameBasedDTO() != null) {
            entity.setName(dto.getNameBasedDTO().getName());
            entity.setEnglish(dto.getNameBasedDTO().getEnglish());
            entity.setAlternative(dto.getNameBasedDTO().getAlternative());
            entity.setShortName(dto.getNameBasedDTO().getShortName());
        }
        entity.setDescription(dto.getDescription());
        return entity;
    };

    // Function to map Entity Name to Base DTO
    public static final BiFunction<BaseDTO, NamedEntity, BaseDTO> MAP_ENTITY_TO_BASE_DTO = (dto, entity) -> {
        dto.setUrl(entity.getUrl());
        dto.setImageUrl(entity.getImageUrl());
        dto.setNameBasedDTO(new NameBasedDTO());
        dto.getNameBasedDTO().setName(entity.getName());
        dto.getNameBasedDTO().setEnglish(entity.getEnglish());
        dto.getNameBasedDTO().setShortName(entity.getShortName());
        dto.getNameBasedDTO().setAlternative(entity.getAlternative());
        dto.setDescription(entity.getDescription());
        if (entity.getUpdatedAt() != null) {
            dto.setUpdatedAt(entity.getUpdatedAt().atZone(ZoneId.of("UTC")).toInstant().truncatedTo(ChronoUnit.MINUTES));
        }
        return dto;
    };

    // Function to map Creature Properties DTO to Entity
    public static final BiFunction<CreaturePropertiesDto, CreatureProperties, CreatureProperties> MAP_CREATURE_PROPERTIES_DTO_TO_ENTITY = (dto, entity) -> {
        entity.setSize(Size.valueOf(dto.getSize()));
        entity.setType(CreatureType.valueOf(dto.getType()));
        entity.setSpeed(entity.getSpeed());
        entity.setFly(dto.getMovementAttributes().getFly());
        entity.setClimb(entity.getClimb());
        entity.setSwim(entity.getSwim());
        entity.setDarkVision(dto.getDarkVision());
        return entity;
    };

    // Function to map Creature Properties Entity to DTO
    public static final BiFunction<CreaturePropertiesDto, CreatureProperties, CreaturePropertiesDto> MAP_ENTITY_TO_CREATURE_PROPERTIES_DTO = (dto, entity) -> {
        dto.setSize(entity.getSize().getName());
        dto.setType(entity.getType().getCyrillicName());
        MovementAttributes movementAttributes = MovementAttributes.builder()
                .base(entity.getSpeed())
                .fly(entity.getFly())
                .climb(entity.getClimb())
                .swim(entity.getSwim())
                .build();
        dto.setMovementAttributes(movementAttributes);
        dto.setDarkVision(entity.getDarkVision());
        return dto;
    };

    // Function to map DTO Source to Entity Source
    public static final BiFunction<SourceResponse, HasSourceEntity, HasSourceEntity> MAP_DTO_SOURCE_TO_ENTITY_SOURCE = (dto, entity) -> {
        String sourceAcronym = dto.getName().getShortName();
        Source source = entity.getSource();
        if (source == null) {
            source = new Source();
        }
        Book book = new Book(sourceAcronym);
        book.setSourceAcronym(sourceAcronym);
        source.setBookInfo(book);
        if (dto.getPage() != null) {
            source.setPage(dto.getPage());
        }
        entity.setSource(source);
        return entity;
    };

    // Function to map Entity Source to DTO Source
    public static final BiFunction<SourceResponse, HasSourceEntity, SourceResponse> MAP_ENTITY_SOURCE_TO_DTO_SOURCE = (dto, entity) -> {
        Source source = entity.getSource();
        if (source != null) {
            NameBasedDTO name = new NameBasedDTO();
            Book bookInfo = source.getBookInfo();
            if (bookInfo != null) {
                name.setEnglish(bookInfo.getEnglishName());
                name.setName(bookInfo.getName());
                name.setShortName(bookInfo.getSourceAcronym());
                name.setAlternative(bookInfo.getAltName());
            }
            STRATEGY_SOURCE_CONSUMER.accept(dto, source);
            dto.setName(name);
            dto.setPage(source.getPage());
        }
        return dto;
    };

    // Function to map Entity to Base DTO with hidden details
    public static final BiFunction<BaseDTO, NamedEntity, BaseDTO> MAP_ENTITY_TO_BASE_DTO_WITH_HIDE_DETAILS = (dto, entity) -> {
        dto.setUrl(entity.getUrl());
        ((DetailableDTO) dto).hideDetails();
        return dto;
    };

    public static final BiFunction<ClassFeatureDto ,ClassFeature, ClassFeatureDto> MAP_ENTITY_TO_DTO_WITH_LEVEL = (dto, entity) -> {
        dto.setLevel(entity.getLevel());
        return dto;
    };
}

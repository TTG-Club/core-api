package club.ttg.dnd5.utills;

import club.ttg.dnd5.dictionary.Size;
import club.ttg.dnd5.dictionary.beastiary.CreatureType;
import club.ttg.dnd5.dto.base.BaseDTO;
import club.ttg.dnd5.dto.base.HasSourceDTO;
import club.ttg.dnd5.dto.base.NameBasedDTO;
import club.ttg.dnd5.dto.base.SourceResponse;
import club.ttg.dnd5.dto.species.CreateSpeciesDto;
import club.ttg.dnd5.dto.species.CreaturePropertiesDto;
import club.ttg.dnd5.dto.species.SpeciesDto;
import club.ttg.dnd5.model.base.CreatureProperties;
import club.ttg.dnd5.model.base.NamedEntity;
import club.ttg.dnd5.model.book.Book;
import club.ttg.dnd5.model.book.Source;
import club.ttg.dnd5.model.species.Species;
import io.jsonwebtoken.lang.Assert;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ConverterTest {
    private BaseDTO baseDTO;
    private NamedEntity namedEntity;
    private CreaturePropertiesDto creaturePropertiesDTO;
    private CreatureProperties creatureProperties;
    private SpeciesDto speciesDTO;
    private Source source;
    @Before
    public void setUp() {
        // Set up the BaseDTO and NamedEntity
        baseDTO = new CreateSpeciesDto();
        baseDTO.setUrl("http://example.com");
        baseDTO.setDescription("Test Description");
        NameBasedDTO nameBasedDTO = new NameBasedDTO();
        nameBasedDTO.setName("Test Name");
        nameBasedDTO.setEnglish("Test English Name");
        nameBasedDTO.setAlternative("Test Alternative Name");
        baseDTO.setNameBasedDTO(nameBasedDTO);

        namedEntity = new Species();
        namedEntity.setUrl("http://example.com");
        namedEntity.setName("Entity Name");
        namedEntity.setEnglish("Entity English Name");
        namedEntity.setAlternative("Entity Alternative Name");
        namedEntity.setDescription("Entity Description");

        // Set up CreaturePropertiesDto and CreatureProperties
        creaturePropertiesDTO = new CreaturePropertiesDto();
        creaturePropertiesDTO.setSize(Size.MEDIUM);
        creaturePropertiesDTO.setType(CreatureType.BEAST);
        creaturePropertiesDTO.setSpeed(30);
        creaturePropertiesDTO.setFly(0);
        creaturePropertiesDTO.setClimb(0);
        creaturePropertiesDTO.setSwim(0);
        creaturePropertiesDTO.setDarkVision(60);

        creatureProperties = new Species();
        creatureProperties.setSize(Size.MEDIUM);
        creatureProperties.setType(CreatureType.BEAST);
        creatureProperties.setSpeed(30);
        creatureProperties.setFly(0);
        creatureProperties.setClimb(0);
        creatureProperties.setSwim(0);
        creatureProperties.setDarkVision(60);

        SourceResponse sourceResponse = new SourceResponse();
        sourceResponse.setSource("PHB");
        sourceResponse.setPage((short) 155);
        speciesDTO = new SpeciesDto();
        speciesDTO.setSourceDTO(sourceResponse);

        source = new Source();
        Book book = new Book();
        book.setSourceAcronym("PHB");
        source.setBookInfo(book);
        source.setPage((short) 155);
    }

    // Test mapping from DTO to Entity for BaseDTO and NamedEntity
    @Test
    public void testMapBaseDTOToEntityName() {
        Species species = new Species();
        NamedEntity result = Converter.MAP_BASE_DTO_TO_ENTITY_NAME.apply(baseDTO, species);

        assertEquals(baseDTO.getUrl(), result.getUrl());
        assertEquals(baseDTO.getNameBasedDTO().getName(), result.getName());
        assertEquals(baseDTO.getNameBasedDTO().getEnglish(), result.getEnglish());
        assertEquals(baseDTO.getNameBasedDTO().getAlternative(), result.getAlternative());
        assertEquals(baseDTO.getDescription(), result.getDescription());
    }

    // Test mapping from Entity to DTO for NamedEntity and BaseDTO
    @Test
    public void testMapEntityToBaseDTO() {
        BaseDTO result = Converter.MAP_ENTITY_TO_BASE_DTO.apply(new SpeciesDto(), namedEntity);

        assertEquals(namedEntity.getUrl(), result.getUrl());
        assertEquals(namedEntity.getName(), result.getNameBasedDTO().getName());
        assertEquals(namedEntity.getEnglish(), result.getNameBasedDTO().getEnglish());
        assertEquals(namedEntity.getAlternative(), result.getNameBasedDTO().getAlternative());
        assertEquals(namedEntity.getDescription(), result.getDescription());
    }

    // Test mapping from CreaturePropertiesDto to CreatureProperties Entity
    @Test
    public void testMapCreaturePropertiesDTOToEntity() {
        CreatureProperties result = Converter.MAP_CREATURE_PROPERTIES_DTO_TO_ENTITY.apply(creaturePropertiesDTO, new Species());

        assertEquals(creaturePropertiesDTO.getSize(), result.getSize());
        assertEquals(creaturePropertiesDTO.getType(), result.getType());
        assertEquals(creaturePropertiesDTO.getSpeed(), result.getSpeed());
        assertEquals(creaturePropertiesDTO.getFly(), result.getFly());
        assertEquals(creaturePropertiesDTO.getClimb(), result.getClimb());
        assertEquals(creaturePropertiesDTO.getSwim(), result.getSwim());
        assertEquals(creaturePropertiesDTO.getDarkVision(), result.getDarkVision());
    }

    // Test mapping from CreatureProperties Entity to CreaturePropertiesDto
    @Test
    public void testMapEntityToCreaturePropertiesDTO() {
        CreaturePropertiesDto result = Converter.MAP_ENTITY_TO_CREATURE_PROPERTIES_DTO.apply(new CreaturePropertiesDto(), creatureProperties);

        assertEquals(creatureProperties.getSize(), result.getSize());
        assertEquals(creatureProperties.getType(), result.getType());
        assertEquals(creatureProperties.getSpeed(), result.getSpeed());
        assertEquals(creatureProperties.getFly(), result.getFly());
        assertEquals(creatureProperties.getClimb(), result.getClimb());
        assertEquals(creatureProperties.getSwim(), result.getSwim());
        assertEquals(creatureProperties.getDarkVision(), result.getDarkVision());
    }

    @Test
    public void testMapDtoSourceToEntitySource() {
        Species species = new Species();
        Converter.MAP_DTO_SOURCE_TO_ENTITY_SOURCE.apply(speciesDTO, species);

        assertEquals((Short) species.getSource().getPage(), speciesDTO.getPage());
        assertEquals(species.getSource().getSourceAcronym(), speciesDTO.getSourceDTO().getSource());
    }

    @Test
    public void testMapEntitySourceToDtoSource() {
        Species species = new Species();
        species.setSource(source);
        HasSourceDTO apply = Converter.MAP_ENTITY_SOURCE_TO_DTO_SOURCE.apply(speciesDTO, species);

        assertEquals((Short) species.getSource().getPage(), apply.getPage());
        assertEquals(species.getSource().getSourceAcronym(), apply.getSource());
    }

    @Test
    public void testMapEntityWithHideDetails() {
        Species species = new Species();
        species.setSource(source);
        species.setSwim(0);
        species.setClimb(50);
        species.setSize(Size.HUGE);
        speciesDTO.setCreatureProperties(new CreaturePropertiesDto());
        Converter.MAP_ENTITY_TO_CREATURE_PROPERTIES_DTO.apply(speciesDTO.getCreatureProperties(), species);
        BaseDTO apply = Converter.MAP_ENTITY_TO_BASE_DTO_WITH_HIDE_DETAILS.apply(speciesDTO, species);
        Assert.isNull(speciesDTO.getCreatureProperties());
    }
}
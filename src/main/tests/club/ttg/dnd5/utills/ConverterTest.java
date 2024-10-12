package club.ttg.dnd5.utills;

import club.ttg.dnd5.dictionary.Size;
import club.ttg.dnd5.dictionary.beastiary.CreatureType;
import club.ttg.dnd5.dto.base.BaseDTO;
import club.ttg.dnd5.dto.base.NameBasedDTO;
import club.ttg.dnd5.dto.species.CreateSpeciesDTO;
import club.ttg.dnd5.dto.species.CreaturePropertiesDTO;
import club.ttg.dnd5.dto.species.SpeciesResponse;
import club.ttg.dnd5.model.base.CreatureProperties;
import club.ttg.dnd5.model.base.NamedEntity;
import club.ttg.dnd5.model.species.Species;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ConverterTest {
    private BaseDTO baseDTO;
    private NamedEntity namedEntity;
    private CreaturePropertiesDTO creaturePropertiesDTO;
    private CreatureProperties creatureProperties;

    @Before
    public void setUp() {
        // Set up the BaseDTO and NamedEntity
        baseDTO = new CreateSpeciesDTO();
        baseDTO.setUrl("http://example.com");
        NameBasedDTO nameBasedDTO = new NameBasedDTO();
        nameBasedDTO.setName("Test Name");
        nameBasedDTO.setEnglish("Test English Name");
        nameBasedDTO.setAlternative("Test Alternative Name");
        nameBasedDTO.setDescription("Test Description");
        baseDTO.setNameBasedDTO(nameBasedDTO);

        namedEntity = new Species();
        namedEntity.setUrl("http://example.com");
        namedEntity.setName("Entity Name");
        namedEntity.setEnglish("Entity English Name");
        namedEntity.setAlternative("Entity Alternative Name");
        namedEntity.setDescription("Entity Description");

        // Set up CreaturePropertiesDTO and CreatureProperties
        creaturePropertiesDTO = new CreaturePropertiesDTO();
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
    }

    // Test mapping from DTO to Entity for BaseDTO and NamedEntity
    @Test
    public void testMapBaseDTOToEntityName() {
        NamedEntity result = Converter.mapBaseDTOToEntityName(baseDTO, new Species());

        assertEquals(baseDTO.getUrl(), result.getUrl());
        assertEquals(baseDTO.getNameBasedDTO().getName(), result.getName());
        assertEquals(baseDTO.getNameBasedDTO().getEnglish(), result.getEnglish());
        assertEquals(baseDTO.getNameBasedDTO().getAlternative(), result.getAlternative());
        assertEquals(baseDTO.getNameBasedDTO().getDescription(), result.getDescription());
    }

    // Test mapping from Entity to DTO for NamedEntity and BaseDTO
    @Test
    public void testMapEntityToBaseDTO() {
        BaseDTO result = Converter.mapEntityToBaseDTO(new SpeciesResponse(), namedEntity);

        assertEquals(namedEntity.getUrl(), result.getUrl());
        assertEquals(namedEntity.getName(), result.getNameBasedDTO().getName());
        assertEquals(namedEntity.getEnglish(), result.getNameBasedDTO().getEnglish());
        assertEquals(namedEntity.getAlternative(), result.getNameBasedDTO().getAlternative());
        assertEquals(namedEntity.getDescription(), result.getNameBasedDTO().getDescription());
    }

    // Test mapping from CreaturePropertiesDTO to CreatureProperties Entity
    @Test
    public void testMapCreaturePropertiesDTOToEntity() {
        CreatureProperties result = Converter.mapCreaturePropertiesDTOToEntity(creaturePropertiesDTO, new Species());

        assertEquals(creaturePropertiesDTO.getSize(), result.getSize());
        assertEquals(creaturePropertiesDTO.getType(), result.getType());
        assertEquals(creaturePropertiesDTO.getSpeed(), result.getSpeed());
        assertEquals(creaturePropertiesDTO.getFly(), result.getFly());
        assertEquals(creaturePropertiesDTO.getClimb(), result.getClimb());
        assertEquals(creaturePropertiesDTO.getSwim(), result.getSwim());
        assertEquals(creaturePropertiesDTO.getDarkVision(), result.getDarkVision());
    }

    // Test mapping from CreatureProperties Entity to CreaturePropertiesDTO
    @Test
    public void testMapEntityToCreaturePropertiesDTO() {
        CreaturePropertiesDTO result = Converter.mapEntityToCreaturePropertiesDTO(new CreaturePropertiesDTO(), creatureProperties);

        assertEquals(creatureProperties.getSize(), result.getSize());
        assertEquals(creatureProperties.getType(), result.getType());
        assertEquals(creatureProperties.getSpeed(), result.getSpeed());
        assertEquals(creatureProperties.getFly(), result.getFly());
        assertEquals(creatureProperties.getClimb(), result.getClimb());
        assertEquals(creatureProperties.getSwim(), result.getSwim());
        assertEquals(creatureProperties.getDarkVision(), result.getDarkVision());
    }
}
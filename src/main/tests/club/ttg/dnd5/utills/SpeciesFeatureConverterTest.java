package club.ttg.dnd5.utills;


import club.ttg.dnd5.dto.base.TagDto;
import club.ttg.dnd5.dto.species.SpeciesFeatureDto;
import club.ttg.dnd5.model.species.Species;
import club.ttg.dnd5.model.species.SpeciesFeature;
import club.ttg.dnd5.utills.species.SpeciesFeatureConverter;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SpeciesFeatureConverterTest {

    @Test
    public void testToEntityFeature() {
        // Arrange
        SpeciesFeatureDto response = new SpeciesFeatureDto();
        // Create tags using TagDto
        List<TagDto> tags = List.of(
                new TagDto("tag1", "value1"),
                new TagDto("tag2", "value2")
        );
        response.setTags(tags);
        response.setDescription("Feature description");
        response.setUrl("http://example.com/species-feature");

        // Act
        SpeciesFeature feature = SpeciesFeatureConverter.toEntityFeature(response);

        // Assert
        assertNotNull(feature);
        assertEquals(response.getDescription(), feature.getFeatureDescription());

        // Compare tags (list of TagDto to Map)
        assertEquals(tags.stream()
                        .collect(java.util.stream.Collectors.toMap(TagDto::getName, TagDto::getValue)),
                feature.getTags());

        assertEquals(response.getUrl(), feature.getUrl());
    }

    @Test
    public void testToDTOFeature() {
        // Arrange
        SpeciesFeature feature = new SpeciesFeature();
        // Create tags using Map directly in the entity
        feature.setTags(Map.of("tag1", "value1", "tag2", "value2"));
        feature.setFeatureDescription("Feature description");
        feature.setUrl("http://example.com/species-feature");

        // Act
        SpeciesFeatureDto response = SpeciesFeatureConverter.toDTOFeature(feature);

        // Assert
        assertNotNull(response);
        assertEquals(feature.getFeatureDescription(), response.getDescription());

        // Convert the map back to a list of TagDto
        List<TagDto> expectedTags = feature.getTags().entrySet().stream()
                .map(entry -> new TagDto(entry.getKey(), entry.getValue()))
                .collect(java.util.stream.Collectors.toList());

        assertEquals(expectedTags, response.getTags());
        assertEquals(feature.getUrl(), response.getUrl());
    }

    @Test
    public void testConvertDTOFeatureIntoEntityFeature() {
        // Arrange
        Species species = new Species();
        SpeciesFeatureDto response = new SpeciesFeatureDto();
        // Create tags using TagDto
        List<TagDto> tags = List.of(
                new TagDto("tag1", "value1"),
                new TagDto("tag2", "value2")
        );
        response.setTags(tags);
        response.setDescription("Feature description");

        // Act
        SpeciesFeatureConverter.convertDTOFeatureIntoEntityFeature(List.of(response), species);

        // Assert
        assertNotNull(species.getFeatures());
        assertEquals(1, species.getFeatures().size());
        SpeciesFeature feature = species.getFeatures().iterator().next();
        assertEquals(response.getDescription(), feature.getFeatureDescription());

        // Convert the list of TagDto to Map for comparison
        assertEquals(tags.stream()
                        .collect(java.util.stream.Collectors.toMap(TagDto::getName, TagDto::getValue)),
                feature.getTags());
    }

    @Test
    public void testConvertEntityFeatureIntoDTOFeature() {
        // Arrange
        SpeciesFeature feature = new SpeciesFeature();
        // Create tags using Map directly in the entity
        feature.setTags(Map.of("tag1", "value1", "tag2", "value2"));
        feature.setFeatureDescription("Feature description");

        // Act
        Collection<SpeciesFeatureDto> response = SpeciesFeatureConverter.convertEntityFeatureIntoDTOFeature(List.of(feature));

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
        SpeciesFeatureDto dto = response.iterator().next();
        assertEquals(feature.getFeatureDescription(), dto.getDescription());

        // Convert the map back to a list of TagDto
        List<TagDto> expectedTags = feature.getTags().entrySet().stream()
                .map(entry -> new TagDto(entry.getKey(), entry.getValue()))
                .collect(java.util.stream.Collectors.toList());

        assertEquals(expectedTags, dto.getTags());
    }
}
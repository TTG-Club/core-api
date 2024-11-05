package club.ttg.dnd5.utills;

import club.ttg.dnd5.dto.species.SpeciesFeatureResponse;
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
        SpeciesFeatureResponse response = new SpeciesFeatureResponse();
        response.setTags(Map.of("tag1", "value1", "tag2", "value2"));
        response.setDescription("Feature description");
        response.setUrl("http://example.com/species-feature");

        // Act
        SpeciesFeature feature = SpeciesFeatureConverter.toEntityFeature(response);

        // Assert
        assertNotNull(feature);
        assertEquals(response.getDescription(), feature.getFeatureDescription());
        assertEquals(response.getTags(), feature.getTags());
        assertEquals(response.getUrl(), feature.getUrl());
    }

    @Test
    public void testToDTOFeature() {
        // Arrange
        SpeciesFeature feature = new SpeciesFeature();
        feature.setTags(Map.of("tag1", "value1", "tag2", "value2"));
        feature.setFeatureDescription("Feature description");
        feature.setUrl("http://example.com/species-feature");

        // Act
        SpeciesFeatureResponse response = SpeciesFeatureConverter.toDTOFeature(feature);

        // Assert
        assertNotNull(response);
        assertEquals(feature.getFeatureDescription(), response.getDescription());
        assertEquals(feature.getTags(), response.getTags());
        assertEquals(feature.getUrl(), response.getUrl());
    }

    @Test
    public void testConvertDTOFeatureIntoEntityFeature() {
        // Arrange
        Species species = new Species();
        SpeciesFeatureResponse response = new SpeciesFeatureResponse();
        response.setTags(Map.of("tag1", "value1", "tag2", "value2"));
        response.setDescription("Feature description");

        // Act
        SpeciesFeatureConverter.convertDTOFeatureIntoEntityFeature(List.of(response), species);

        // Assert
        assertNotNull(species.getFeatures());
        assertEquals(1, species.getFeatures().size());
        SpeciesFeature feature = species.getFeatures().iterator().next();
        assertEquals(response.getDescription(), feature.getFeatureDescription());
        assertEquals(response.getTags(), feature.getTags());
    }

    @Test
    public void testConvertEntityFeatureIntoDTOFeature() {
        // Arrange
        SpeciesFeature feature = new SpeciesFeature();
        feature.setTags(Map.of("tag1", "value1", "tag2", "value2"));
        feature.setFeatureDescription("Feature description");

        // Act
        Collection<SpeciesFeatureResponse> response = SpeciesFeatureConverter.convertEntityFeatureIntoDTOFeature(List.of(feature));

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
        SpeciesFeatureResponse dto = response.iterator().next();
        assertEquals(feature.getFeatureDescription(), dto.getDescription());
        assertEquals(feature.getTags(), dto.getTags());
    }
}

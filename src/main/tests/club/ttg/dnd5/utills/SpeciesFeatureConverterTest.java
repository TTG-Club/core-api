package club.ttg.dnd5.utills;

import club.ttg.dnd5.dto.species.SpeciesFeatureDto;
import club.ttg.dnd5.model.base.Tag;
import club.ttg.dnd5.model.species.Species;
import club.ttg.dnd5.model.species.SpeciesFeature;
import club.ttg.dnd5.utills.species.SpeciesFeatureConverter;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SpeciesFeatureConverterTest {

    @Test
    public void testToEntityFeature() {
        // Arrange
        SpeciesFeatureDto response = new SpeciesFeatureDto();
        // Create tags as a Set<String>
        Set<String> tags = Set.of("tag1", "tag2");
        response.setTags(tags);
        response.setDescription("Feature description");
        response.setUrl("http://example.com/species-feature");

        // Act
        SpeciesFeature feature = SpeciesFeatureConverter.toEntityFeature(response);

        // Assert
        assertNotNull(feature);
        assertEquals(response.getDescription(), feature.getFeatureDescription());

        // Compare tags (Set<String> to Set<Tag>)
        Set<Tag> expectedTags = tags.stream()
                .map(Tag::new)
                .collect(Collectors.toSet());
        assertEquals(expectedTags, feature.getTags());

        assertEquals(response.getUrl(), feature.getUrl());
    }

    @Test
    public void testToDTOFeature() {
        // Arrange
        SpeciesFeature feature = new SpeciesFeature();
        // Create tags as Set<Tag> directly in the entity
        Set<Tag> tags = Set.of(new Tag("tag1"), new Tag("tag2"));
        feature.setTags(tags);
        feature.setFeatureDescription("Feature description");
        feature.setUrl("http://example.com/species-feature");

        // Act
        SpeciesFeatureDto response = SpeciesFeatureConverter.toDTOFeature(feature);

        // Assert
        assertNotNull(response);
        assertEquals(feature.getFeatureDescription(), response.getDescription());

        // Convert the Set<Tag> to Set<String>
        Set<String> expectedTags = tags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        assertEquals(expectedTags, response.getTags());
        assertEquals(feature.getUrl(), response.getUrl());
    }

    @Test
    public void testConvertDTOFeatureIntoEntityFeature() {
        // Arrange
        Species species = new Species();
        SpeciesFeatureDto response = new SpeciesFeatureDto();
        // Create tags as Set<String>
        Set<String> tags = Set.of("tag1", "tag2");
        response.setTags(tags);
        response.setDescription("Feature description");

        // Act
        SpeciesFeatureConverter.convertDTOFeatureIntoEntityFeature(List.of(response), species);

        // Assert
        assertNotNull(species.getFeatures());
        assertEquals(1, species.getFeatures().size());
        SpeciesFeature feature = species.getFeatures().iterator().next();
        assertEquals(response.getDescription(), feature.getFeatureDescription());

        // Compare tags (Set<String> to Set<Tag>)
        Set<Tag> expectedTags = tags.stream()
                .map(Tag::new)
                .collect(Collectors.toSet());
        assertEquals(expectedTags, feature.getTags());
    }

    @Test
    public void testConvertEntityFeatureIntoDTOFeature() {
        // Arrange
        SpeciesFeature feature = new SpeciesFeature();
        // Create tags as Set<Tag>
        Set<Tag> tags = Set.of(new Tag("tag1"), new Tag("tag2"));
        feature.setTags(tags);
        feature.setFeatureDescription("Feature description");

        // Act
        Collection<SpeciesFeatureDto> response = SpeciesFeatureConverter.convertEntityFeatureIntoDTOFeature(List.of(feature));

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
        SpeciesFeatureDto dto = response.iterator().next();
        assertEquals(feature.getFeatureDescription(), dto.getDescription());

        // Convert the Set<Tag> to Set<String>
        Set<String> expectedTags = tags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        assertEquals(expectedTags, dto.getTags());
    }
}

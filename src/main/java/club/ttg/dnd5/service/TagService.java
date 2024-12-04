package club.ttg.dnd5.service;

import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.model.base.Tag;
import club.ttg.dnd5.model.species.SpeciesFeature;
import club.ttg.dnd5.repository.SpeciesFeatureRepository;
import club.ttg.dnd5.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final SpeciesFeatureRepository speciesFeatureRepository;

    public Tag createTag(String tagName) {
        Tag tag = new Tag(tagName);
        return tagRepository.save(tag);
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public Optional<Tag> getTagByName(String name) {
        return tagRepository.findByNameIgnoreCase(name);
    }

    // Добавление тега к SpeciesFeature
    public SpeciesFeature addTagToFeature(String featureId, Long tagId) {
        SpeciesFeature feature = speciesFeatureRepository.findById(featureId)
                .orElseThrow(() -> new EntityNotFoundException("Feature not found"));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found"));

        feature.getTags().add(tag);
        return speciesFeatureRepository.save(feature);
    }

    // Получить все features по тегу
    public List<SpeciesFeature> getSpeciesFeaturesByTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found"));
        return tag.getSpeciesFeatures().stream().collect(Collectors.toList());
    }

    // Получить все теги для Feature
    public Set<Tag> getTagsForFeature(String featureUrl) {
        SpeciesFeature feature = speciesFeatureRepository.findById(featureUrl)
                .orElseThrow(() -> new EntityNotFoundException("Feature not found"));
        return feature.getTags();
    }
}

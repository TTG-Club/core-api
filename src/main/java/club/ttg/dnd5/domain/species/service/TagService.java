package club.ttg.dnd5.domain.species.service;

import club.ttg.dnd5.domain.common.repository.TagRepository;
import club.ttg.dnd5.domain.species.repository.SpeciesFeatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final SpeciesFeatureRepository speciesFeatureRepository;

//    public Tag createTag(String tagName) {
//        Tag tag = new Tag(tagName);
//        return tagRepository.save(tag);
//    }
//
//    public List<Tag> getAllTags() {
//        return tagRepository.findAll();
//    }
//
//    public Optional<Tag> getTagByName(String name) {
//        return tagRepository.findByNameIgnoreCase(name);
//    }
//
//    // Добавление тега к SpeciesFeature
//    public SpeciesFeature addTagToFeature(String featureId, Long tagId) {
//        SpeciesFeature feature = speciesFeatureRepository.findById(featureId)
//                .orElseThrow(() -> new EntityNotFoundException("Feature not found"));
//        Tag tag = tagRepository.findById(tagId)
//                .orElseThrow(() -> new EntityNotFoundException("Tag not found"));
//        feature.getTags().add(tag);
//        return speciesFeatureRepository.save(feature);
//    }
//
//    // Получить все features по тегу
//    public List<SpeciesFeature> getSpeciesFeaturesByTag(Long tagId) {
//        Tag tag = tagRepository.findById(tagId)
//                .orElseThrow(() -> new EntityNotFoundException("Tag not found"));
//        return tag.getSpeciesFeatures().stream().collect(Collectors.toList());
//    }
//
//    // Получить все теги для Feature
//    public Set<Tag> getTagsForFeature(String featureUrl) {
//        SpeciesFeature feature = speciesFeatureRepository.findById(featureUrl)
//                .orElseThrow(() -> new EntityNotFoundException("Feature not found"));
//        return feature.getTags();
//    }
}

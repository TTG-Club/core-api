package club.ttg.dnd5.service.species;

import club.ttg.dnd5.dto.engine.SearchRequest;
import club.ttg.dnd5.dto.species.SpeciesFeatureResponse;
import club.ttg.dnd5.dto.species.SpeciesResponse;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.mapper.species.SpeciesMapper;
import club.ttg.dnd5.model.species.Species;
import club.ttg.dnd5.model.species.SpeciesFeature;
import club.ttg.dnd5.repository.SpeciesRepository;
import club.ttg.dnd5.specification.SpeciesSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpeciesService {
    private final SpeciesRepository speciesRepository;
    private static final SpeciesMapper speciesMapper = SpeciesMapper.INSTANCE;

    public SpeciesResponse findById(String url) {
        return speciesRepository.findById(url)
                .map(speciesMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Species not found with url: " + url));
    }

    @Transactional
    public SpeciesResponse save(SpeciesResponse speciesResponse) {
        Species species = speciesMapper.toEntity(speciesResponse);
        Species savedSpecies = speciesRepository.save(species);
        return speciesMapper.toDTO(savedSpecies);
    }

    @Transactional
    public SpeciesResponse update(String oldUrl, SpeciesResponse speciesResponse) {
        if (speciesRepository.existsById(oldUrl)) {
            if (!oldUrl.equals(speciesResponse.getUrl())) {
                speciesRepository.deleteById(oldUrl);
            }

            Species species = speciesMapper.toEntity(speciesResponse);

            if (speciesResponse.getParent() != null) {
                Species parentSpecies = speciesMapper.toEntity(speciesResponse.getParent());
                species.setParent(parentSpecies);
            }

            if (speciesResponse.getSubSpecies() != null) {
                Collection<Species> subSpecies = speciesResponse.getSubSpecies().stream()
                        .map(speciesMapper::toEntity)
                        .peek(sub -> sub.setParent(species))  // Set parent for each sub-species
                        .toList();
                species.setSubSpecies(subSpecies);
            }

            if (speciesResponse.getFeatures() != null) {
                Collection<SpeciesFeature> features = speciesResponse.getFeatures().stream()
                        .map(this::toEntityFeature)  // Custom conversion method
                        .toList();
                species.setFeatures(features);
            }

            Species updatedSpecies = speciesRepository.save(species);

            return speciesMapper.toDTO(updatedSpecies);
        } else {
            throw new EntityNotFoundException("Species with URL " + oldUrl + " does not exist.");
        }
    }

    public List<SpeciesResponse> searchSpecies(SearchRequest request) {
        SpeciesSpecification speciesSpecification = new SpeciesSpecification();
        Specification<Species> spec = speciesSpecification.toSpecification(request);

        Pageable pageable = PageRequest.of(
                Optional.ofNullable(request.getPage()).orElse(0),
                Optional.ofNullable(request.getSize()).orElse(10)
        );

        Page<Species> speciesPage = speciesRepository.findAll(spec, pageable);

        return speciesMapper.convertNotDetailList(speciesPage.getContent());
    }

    private SpeciesFeature toEntityFeature(SpeciesFeatureResponse response) {
        if (response == null) {
            return null;
        }

        SpeciesFeature speciesFeature = new SpeciesFeature();
        speciesFeature.setUrl(response.getUrl());
        speciesFeature.setName(response.getEntries().getName());

        List<String> entries = response.getEntries().getEntries().stream()
                .map(Object::toString)
                .toList();
        speciesFeature.setEntries(entries);

        return speciesFeature;
    }
}
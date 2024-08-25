package club.ttg.dnd5.service.species;

import club.ttg.dnd5.dto.engine.SearchRequest;
import club.ttg.dnd5.dto.species.SpeciesResponse;
import club.ttg.dnd5.exception.StorageException;
import club.ttg.dnd5.mapper.species.SpeciesMapper;
import club.ttg.dnd5.model.species.Species;
import club.ttg.dnd5.repository.SpeciesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SpeciesService {
    private final SpeciesRepository speciesRepository;
    private final SpeciesMapper speciesMapper = SpeciesMapper.INSTANCE;

    @Autowired
    public SpeciesService(SpeciesRepository speciesRepository) {
        this.speciesRepository = speciesRepository;
    }

    public List<SpeciesResponse> findAll() {
        return speciesRepository.findAll().stream()
                .map(speciesMapper::toEntity)
                .collect(Collectors.toList());
    }

    public SpeciesResponse findById(String url) {
        return speciesRepository.findById(url)
                .map(speciesMapper::toEntity)
                .orElseThrow(() -> new StorageException("Species not found with url: " + url));
    }


    public SpeciesResponse save(SpeciesResponse speciesResponse) {
        Species species = speciesMapper.toDTO(speciesResponse);
        Species savedSpecies = speciesRepository.save(species);
        return speciesMapper.toEntity(savedSpecies);
    }

    public SpeciesResponse update(SpeciesResponse speciesResponse) {
        if (speciesRepository.existsById(speciesResponse.getUrl())) {
            Species species = speciesMapper.toDTO(speciesResponse);
            Species updatedSpecies = speciesRepository.save(species);
            return speciesMapper.toEntity(updatedSpecies);
        } else {
            throw new StorageException("Species with url " + speciesResponse.getUrl() + " does not exist.");
        }
    }

    public List<SpeciesResponse> searchSpecies(SearchRequest request) {
        Specification<Species> spec = SpeciesSpecification.buildSpecification(request);

        Pageable pageable = PageRequest.of(
                Optional.ofNullable(request.getPage()).orElse(0),
                Optional.ofNullable(request.getSize()).orElse(10)
        );

        Page<Species> speciesPage = speciesRepository.findAll(spec, pageable);

        return speciesPage.stream()
                .map(speciesMapper::toEntity)
                .toList();
    }
}
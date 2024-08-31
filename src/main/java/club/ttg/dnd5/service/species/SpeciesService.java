package club.ttg.dnd5.service.species;

import club.ttg.dnd5.dto.engine.SearchRequest;
import club.ttg.dnd5.dto.species.SpeciesResponse;
import club.ttg.dnd5.exception.StorageException;
import club.ttg.dnd5.mapper.species.SpeciesMapper;
import club.ttg.dnd5.model.species.Species;
import club.ttg.dnd5.repository.SpeciesRepository;
import club.ttg.dnd5.specification.SpeciesSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new StorageException("Species not found with url: " + url));
    }


    public SpeciesResponse save(SpeciesResponse speciesResponse) {
        Species species = speciesMapper.toEntity(speciesResponse);
        Species savedSpecies = speciesRepository.save(species);
        return speciesMapper.toDTO(savedSpecies);
    }

    public SpeciesResponse update(SpeciesResponse speciesResponse) {
        if (speciesRepository.existsById(speciesResponse.getUrl())) {
            Species species = speciesMapper.toEntity(speciesResponse);
            Species updatedSpecies = speciesRepository.save(species);
            return speciesMapper.toDTO(updatedSpecies);
        } else {
            throw new StorageException("Species with url " + speciesResponse.getUrl() + " does not exist.");
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
}
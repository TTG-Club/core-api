package club.ttg.dnd5.service.species;

import club.ttg.dnd5.dto.EntryDto;
import club.ttg.dnd5.dto.engine.SearchRequest;
import club.ttg.dnd5.dto.species.CreateSpeciesDTO;
import club.ttg.dnd5.dto.species.SpeciesFeatureResponse;
import club.ttg.dnd5.dto.species.SpeciesResponse;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.model.species.Species;
import club.ttg.dnd5.model.species.SpeciesFeature;
import club.ttg.dnd5.repository.SpeciesRepository;
import club.ttg.dnd5.spec.SpeciesSpecification;
import club.ttg.dnd5.utills.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SpeciesService {
    private final SpeciesRepository speciesRepository;

    public SpeciesResponse findById(String url) {
        Species species = speciesRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Species not found with URL: " + url));
        return toDTO(species);
    }

    @Transactional
    public SpeciesResponse save(CreateSpeciesDTO createSpeciesDTO) {
        Species species = new Species();
        Converter.mapBaseDTOToEntityName(createSpeciesDTO, species);
        Converter.mapCreaturePropertiesDTOToEntity(createSpeciesDTO.getCreatureProperties(), species);
        if (createSpeciesDTO.isParent()) {
            species.setParent(species);
        } else {
            species.setParent(null);
        }
        //TODO обработка features
        Species save = speciesRepository.save(species);
        return toDTO(save);
    }

    public SpeciesResponse addParent(String speciesUrl, String speciesParentUrl) {
        Species species = findByUrl(speciesUrl);
        Species parent = findByUrl(speciesParentUrl);

        species.setParent(parent);

        if (parent.getSubSpecies() == null) {
            parent.setSubSpecies(new ArrayList<>());
        }
        parent.getSubSpecies().add(species);

        return toDTO(speciesRepository.save(species));
    }

    public SpeciesResponse addSubSpecies(String speciesUrl, List<String> subSpeciesUrls) {
        Species species = findByUrl(speciesUrl);
        List<Species> subSpeciesEntities = new ArrayList<>();

        for (String url : subSpeciesUrls) {
            Species subSpecies = findByUrl(url);
            subSpeciesEntities.add(subSpecies);

            // Set this species as a parent for the sub-species
            subSpecies.setParent(species);
        }

        // Set the sub-species for the species
        species.setSubSpecies(subSpeciesEntities);

        return toDTO(speciesRepository.save(species));
    }

    @Transactional
    public SpeciesResponse update(String oldUrl, SpeciesResponse speciesResponse) {
        if (speciesRepository.existsById(oldUrl)) {
            if (!oldUrl.equals(speciesResponse.getUrl())) {
                speciesRepository.deleteById(oldUrl);
            }
            return getSpeciesResponse(speciesResponse);
        } else {
            throw new EntityNotFoundException("Species with URL " + oldUrl + " does not exist.");
        }
    }

    private SpeciesResponse getSpeciesResponse(SpeciesResponse speciesResponse) {
        Species species = toEntity(speciesResponse);
        fillSpecies(speciesResponse, species);
        Species updatedSpecies = speciesRepository.save(species);
        return toDTO(updatedSpecies);
    }

    private void fillSpecies(SpeciesResponse speciesResponse, Species species) {
        if (speciesResponse.getSubSpeciesUrls() != null) {
            List<Species> subSpecies = speciesResponse.getSubSpeciesUrls().stream()
                    .map(this::findByUrl)
                    .toList();
            species.setSubSpecies(subSpecies);
        }

        if (speciesResponse.getFeatures() != null) {
            Collection<SpeciesFeature> features = speciesResponse.getFeatures().stream()
                    .map(this::toEntityFeature)
                    .toList();
            species.setFeatures(features);
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
        return speciesPage.getContent().stream()
                .map(this::toDTO)
                .toList();
    }

    private Species findByUrl(String url) {
        return speciesRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Species not found with URL: " + url));
    }

    //метод отвечающий за родителя
    private void fillParent(Species species, SpeciesResponse speciesResponse) {
        Species parentSpecies = null;
        //означает, что текущий вид будет будущим родителем, для других видов
        String parentUrl = speciesResponse.getParentUrl();
        if (parentUrl.equals(speciesResponse.getUrl())) {
            species.setParent(null);
        } else {
            parentSpecies = findByUrl(parentUrl);
        }
        species.setParent(parentSpecies);
    }

    private Species toEntity(SpeciesResponse dto) {
        Species species = new Species();
        species.setUrl(dto.getUrl());
        Converter.mapBaseDTOToEntityName(dto, species);
        Converter.mapDTOSourceToEntitySource(dto.getSource(), species);
        Converter.mapCreaturePropertiesDTOToEntity(dto.getCreatureProperties(), species);
        // Handle parent
        if (dto.getParentUrl() != null) {
            fillParent(species, dto);
        }
        // Handle subSpecies
        fillSpecies(dto, species);
        return species;
    }

    private SpeciesResponse toDTO(Species species) {
        SpeciesResponse dto = new SpeciesResponse();
        Converter.mapEntityToBaseDTO(dto, species);
        Converter.mapEntityToCreaturePropertiesDTO(dto.getCreatureProperties(), species);
        Converter.mapEntitySourceToDTOSource(dto.getSource(), species);

        handleParentAndChild(species, dto);

        // Handle features
        if (species.getFeatures() != null) {
            Collection<SpeciesFeatureResponse> features = species.getFeatures().stream()
                    .map(this::toDTOFeature)
                    .toList();
            dto.setFeatures(features);
        }

        return dto;
    }

    //TODO Добавить корректную поддержку Feature
    private SpeciesFeature toEntityFeature(SpeciesFeatureResponse response) {
        if (response == null) {
            return null;
        }

        SpeciesFeature speciesFeature = new SpeciesFeature();
        speciesFeature.setUrl(response.getUrl());
        speciesFeature.setName(response.getEntries().getName());
        speciesFeature.setEntries(response.getEntries().getEntries().stream()
                .map(Object::toString)
                .toList());

        return speciesFeature;
    }

    private SpeciesFeatureResponse toDTOFeature(SpeciesFeature feature) {
        if (feature == null) {
            return null;
        }

        SpeciesFeatureResponse dto = new SpeciesFeatureResponse();
        dto.setUrl(feature.getUrl());
        EntryDto entries = new EntryDto();
        entries.setName(feature.getName());
        entries.setEntries(Collections.singletonList(feature.getEntries()));
        dto.setEntries(entries);

        return dto;
    }

    private void handleParentAndChild(Species species, SpeciesResponse dto) {
        if (species.getParent() != null) {
            SpeciesResponse parentDTO = new SpeciesResponse();
            parentDTO.setUrl(species.getParent().getUrl());
        }

        // Handle subSpecies
        if (species.getSubSpecies() != null) {
            List<String> subSpeciesUrls = species.getSubSpecies().stream()
                    .map(Species::getUrl)
                    .toList();
            dto.setSubSpeciesUrls(subSpeciesUrls);
        }
    }
}
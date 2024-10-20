package club.ttg.dnd5.service.species;

import club.ttg.dnd5.dto.engine.SearchRequest;
import club.ttg.dnd5.dto.species.CreateSpeciesDTO;
import club.ttg.dnd5.dto.species.SpeciesFeatureResponse;
import club.ttg.dnd5.dto.species.SpeciesResponse;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.model.book.Book;
import club.ttg.dnd5.model.book.Source;
import club.ttg.dnd5.model.species.Species;
import club.ttg.dnd5.model.species.SpeciesFeature;
import club.ttg.dnd5.repository.SourceRepository;
import club.ttg.dnd5.repository.SpeciesFeatureRepository;
import club.ttg.dnd5.repository.SpeciesRepository;
import club.ttg.dnd5.repository.book.BookRepository;
import club.ttg.dnd5.utills.Converter;
import club.ttg.dnd5.utills.species.SpeciesFeatureConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SpeciesService {
    private final SpeciesRepository speciesRepository;
    private final SourceRepository sourceRepository;
    private final BookRepository bookRepository;
    private final SpeciesFeatureRepository speciesFeatureRepository;

    private static void fillParent(CreateSpeciesDTO createSpeciesDTO, Species species) {
        if (createSpeciesDTO.isParent()) {
            species.setParent(species);
        } else {
            species.setParent(null);
        }
    }

    public SpeciesResponse findById(String url) {
        Species species = speciesRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Species not found with URL: " + url));
        return toDTO(species, false);
    }

    @Transactional
    public SpeciesResponse save(CreateSpeciesDTO createSpeciesDTO) {
        Species species = new Species();
        //base
        Converter.mapBaseDTOToEntityName(createSpeciesDTO, species);
        Converter.mapCreaturePropertiesDTOToEntity(createSpeciesDTO.getCreatureProperties(), species);
        //source
        Converter.mapDTOSourceToEntitySource(createSpeciesDTO, species);
        validateAndSaveSource(species.getSource());

        //feature
        saveSpeciesFeatures(createSpeciesDTO, species);


        fillParent(createSpeciesDTO, species);
        Species save = speciesRepository.save(species);
        return toDTO(save, false);
    }

    private void validateAndSaveSource(Source source) {
        if (source != null) {
            Optional<Book> optionalBook = bookRepository.findById(source.getSourceAcronym());
            if (optionalBook.isPresent()) {
                source.setBookInfo(optionalBook.get());
                sourceRepository.save(source);
            } else {
                throw new EntityNotFoundException("Book not found with ID: " + source.getId());
            }
        }
    }

    public List<SpeciesResponse> getSubSpeciesByParentUrl(String parentUrl) {
        Species parentSpecies = speciesRepository.findById(parentUrl)
                .orElseThrow(() -> new EntityNotFoundException("Parent species not found for URL: " + parentUrl));

        List<Species> subSpeciesList = speciesRepository.findByParent(parentSpecies);
        return subSpeciesList.stream()
                .map(species -> toDTO(species, true))
                .toList();
    }

    public List<SpeciesResponse> getAllRelatedSpeciesBySubSpeciesUrl(String subSpeciesUrl) {
        Species subSpecies = speciesRepository.findById(subSpeciesUrl)
                .orElseThrow(() -> new EntityNotFoundException("Sub-species not found for URL: " + subSpeciesUrl));

        List<Species> relatedSpecies = new ArrayList<>();

        relatedSpecies.add(subSpecies);

        Species parentSpecies = subSpecies.getParent();
        if (parentSpecies != null) {
            relatedSpecies.add(parentSpecies);
        }

        Collection<Species> subSpeciesList = subSpecies.getSubSpecies();
        if (subSpeciesList != null && !subSpeciesList.isEmpty()) {
            relatedSpecies.addAll(new ArrayList<>(subSpeciesList)); // Convert Collection to List
        }
        return relatedSpecies.stream()
                .map(species -> toDTO(species, true))
                .toList();
    }

    public SpeciesResponse addParent(String speciesUrl, String speciesParentUrl) {
        Species species = findByUrl(speciesUrl);
        Species parent = findByUrl(speciesParentUrl);

        species.setParent(parent);

        if (parent.getSubSpecies() == null) {
            parent.setSubSpecies(new ArrayList<>());
        }
        parent.getSubSpecies().add(species);

        return toDTO(speciesRepository.save(species), false);
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

        return toDTO(speciesRepository.save(species), false);
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
        return toDTO(updatedSpecies, false);
    }

    public List<SpeciesResponse> searchSpecies(SearchRequest request) {
//        SpeciesSpecification speciesSpecification = new SpeciesSpecification();
//        Specification<Species> spec = speciesSpecification.toSpecification(request);
//
//        Pageable pageable = PageRequest.of(
//                Optional.ofNullable(request.getPage()).orElse(0),
//                Optional.ofNullable(request.getSize()).orElse(10)
//        );
//
//        Page<Species> speciesPage = speciesRepository.findAll(spec, pageable);
//        return speciesPage.getContent().stream()
//                .map(species -> toDTO(species, true))
//                .toList();
        return Collections.emptyList();
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
        Converter.mapDTOSourceToEntitySource(dto.getSourceDTO(), species);
        Converter.mapCreaturePropertiesDTOToEntity(dto.getCreatureProperties(), species);
        // Handle parent
        if (dto.getParentUrl() != null) {
            fillParent(species, dto);
        }
        // Handle subSpecies
        fillSpecies(dto, species);
        return species;
    }

    private SpeciesResponse toDTO(Species species, boolean hideDetails) {
        SpeciesResponse dto = new SpeciesResponse();
        if (hideDetails) {
            Converter.mapEntityToBaseDTOWithHideDetails(dto, species);
        } else {
            Converter.mapEntityToBaseDTO(dto, species);
        }
        Converter.mapEntitySourceToDTOSource(dto.getSourceDTO(), species);

        //creatureProperties
        Converter.mapEntityToCreaturePropertiesDTO(dto.getCreatureProperties(), species);
        dto.getCreatureProperties().setSourceResponse((dto.getSourceDTO()));

        handleParentAndChild(species, dto);

        // Handle features
        Collection<SpeciesFeature> features = species.getFeatures();
        if (features != null) {
            Collection<SpeciesFeatureResponse> speciesFeatureResponses =
                    SpeciesFeatureConverter.convertEntityFeatureIntoDTOFeature(features);
            dto.setFeatures(speciesFeatureResponses);
        }

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

    private void fillSpecies(SpeciesResponse speciesResponse, Species species) {
        if (speciesResponse.getSubSpeciesUrls() != null) {
            List<Species> subSpecies = speciesResponse.getSubSpeciesUrls().stream()
                    .map(this::findByUrl)
                    .toList();
            species.setSubSpecies(subSpecies);
        }
    }

    @Transactional
    public void saveSpeciesFeatures(CreateSpeciesDTO createSpeciesDTO, Species species) {
        SpeciesFeatureConverter.convertDTOFeatureIntoEntityFeature(createSpeciesDTO.getFeatures(), species);
        Collection<SpeciesFeature> features = species.getFeatures();
        if (features != null && !features.isEmpty()) {
            features.stream()
                    .map(SpeciesFeature::getSource)
                    .filter(Objects::nonNull)
                    .forEach(this::validateAndSaveSource);
            speciesFeatureRepository.saveAll(features);
        }
    }
}
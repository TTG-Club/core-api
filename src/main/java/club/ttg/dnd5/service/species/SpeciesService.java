package club.ttg.dnd5.service.species;

import club.ttg.dnd5.dto.species.CreateSpeciesDto;
import club.ttg.dnd5.dto.species.SpeciesDto;
import club.ttg.dnd5.exception.ApiException;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SpeciesService {
    private final SpeciesRepository speciesRepository;
    private final SourceRepository sourceRepository;
    private final BookRepository bookRepository;
    private final SpeciesFeatureRepository speciesFeatureRepository;

    // Public methods
    public SpeciesDto findById(String url) {
        return speciesRepository.findById(url)
                .map(species -> toDTO(species, false))
                .orElseThrow(() -> new EntityNotFoundException(url));
    }

    public List<SpeciesDto> getAllSpecies() {
        return speciesRepository.findAll()
                .stream()
                .map(species -> toDTO(species, true))
                .toList();
    }

    @Transactional
    public SpeciesDto save(CreateSpeciesDto createSpeciesDTO) {
        Species species = new Species();
        Converter.MAP_BASE_DTO_TO_ENTITY_NAME.apply(createSpeciesDTO, species);
        Converter.MAP_CREATURE_PROPERTIES_DTO_TO_ENTITY.apply(createSpeciesDTO.getCreatureProperties(), species);
        Converter.MAP_DTO_SOURCE_TO_ENTITY_SOURCE.apply(createSpeciesDTO, species);

        validateAndSaveSource(species.getSource());
        saveSpeciesFeatures(createSpeciesDTO, species);
        species.setParent(createSpeciesDTO.isParent() ? species : null);

        Species save = speciesRepository.save(species);
        return toDTO(save, false);
    }

    public List<SpeciesDto> getSubSpeciesByParentUrl(String parentUrl) {
        return speciesRepository.findById(parentUrl)
                .filter(species -> !species.isHiddenEntity())
                .map(speciesRepository::findByParent)
                .orElseThrow(() -> new EntityNotFoundException("Parent species not found for URL: " + parentUrl))
                .stream()
                .map(species -> toDTO(species, true))
                .toList();
    }

    public List<SpeciesDto> getAllRelatedSpeciesBySubSpeciesUrl(String subSpeciesUrl) {
        Species subSpecies = speciesRepository.findById(subSpeciesUrl)
                .orElseThrow(() -> new EntityNotFoundException("Sub-species not found for URL: " + subSpeciesUrl));

        return Stream.concat(
                        Stream.of(subSpecies),
                        Stream.concat(
                                Stream.ofNullable(subSpecies.getParent()),
                                subSpecies.getSubSpecies() != null ? subSpecies.getSubSpecies().stream() : Stream.empty()
                        )
                )
                .filter(species -> !species.isHiddenEntity())
                .map(species -> toDTO(species, true))
                .toList();
    }

    public SpeciesDto addParent(String speciesUrl, String speciesParentUrl) {
        Species species = findByUrl(speciesUrl);
        Species parent = findByUrl(speciesParentUrl);
        //на этапе save, мы делаем ссылку на самого себя, если это родитель
        //тут же мы проверяем это утверждение.
        if (parent.getParent().equals(parent)) {
            species.setParent(parent);

            Optional.ofNullable(parent.getSubSpecies())
                    .orElseGet(() -> {
                        parent.setSubSpecies(new ArrayList<>());
                        return parent.getSubSpecies();
                    })
                    .add(species);

            return toDTO(speciesRepository.save(species), false);
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "This is not a parent Species");
        }
    }

    @Transactional
    public SpeciesDto update(String oldUrl, SpeciesDto speciesDTO) {
        if (speciesRepository.existsById(oldUrl)) {
            if (!oldUrl.equals(speciesDTO.getUrl())) {
                speciesRepository.deleteById(oldUrl);
            }
            return getSpeciesResponse(speciesDTO);
        } else {
            throw new EntityNotFoundException("Species with URL " + oldUrl + " does not exist.");
        }
    }

    public SpeciesDto addSubSpecies(String speciesUrl, List<String> subSpeciesUrls) {
        Species species = findByUrl(speciesUrl);

        // Set parent in the map step
        List<Species> subSpeciesEntities = subSpeciesUrls.stream()
                .map(url -> {
                    Species subSpecies = findByUrl(url);
                    subSpecies.setParent(species);
                    return subSpecies;
                })
                .toList();

        species.setSubSpecies(subSpeciesEntities);
        return toDTO(speciesRepository.save(species), false);
    }

    // Private methods
    private Species findByUrl(String url) {
        return speciesRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Species not found with URL: " + url));
    }

    private void validateAndSaveSource(Source source) {
        if (source != null) {
            Book book = bookRepository.findById(source.getSourceAcronym())
                    .orElseThrow(() -> new EntityNotFoundException("Book not found with ID: " + source.getId()));
            source.setBookInfo(book);
            sourceRepository.save(source);
        }
    }

    private Species toEntity(SpeciesDto dto) {
        Species species = new Species();
        species.setUrl(dto.getUrl());
        Converter.MAP_BASE_DTO_TO_ENTITY_NAME.apply(dto, species);
        Converter.MAP_DTO_SOURCE_TO_ENTITY_SOURCE.apply(dto.getSourceDTO(), species);
        Converter.MAP_CREATURE_PROPERTIES_DTO_TO_ENTITY.apply(dto.getCreatureProperties(), species);

        if (dto.getParentUrl() != null) {
            species.setParent(dto.getParentUrl().equals(dto.getUrl()) ? null : findByUrl(dto.getParentUrl()));
        }

        fillSpecies(dto, species);
        return species;
    }

    private SpeciesDto toDTO(Species species, boolean hideDetails) {
        SpeciesDto dto = new SpeciesDto();
        if (hideDetails) {
            Converter.MAP_ENTITY_TO_BASE_DTO_WITH_HIDE_DETAILS.apply(dto, species);
        } else {
            Converter.MAP_ENTITY_TO_BASE_DTO.apply(dto, species);
            Converter.MAP_ENTITY_SOURCE_TO_DTO_SOURCE.apply(dto.getSourceDTO(), species);
            Converter.MAP_ENTITY_TO_CREATURE_PROPERTIES_DTO.apply(dto.getCreatureProperties(), species);
            dto.getCreatureProperties().setSourceResponse(dto.getSourceDTO());
            handleParentAndChild(species, dto);
            Collection<SpeciesFeature> features = species.getFeatures();
            if (features != null) {
                dto.setFeatures(SpeciesFeatureConverter.convertEntityFeatureIntoDTOFeature(features));
            }
        }
        return dto;
    }

    private void handleParentAndChild(Species species, SpeciesDto dto) {
        if (species.getParent() != null) {
            dto.setParentUrl(species.getParent().getUrl());
        }

        if (species.getSubSpecies() != null) {
            dto.setSubSpeciesUrls(species.getSubSpecies().stream()
                    .map(Species::getUrl)
                    .toList());
        }
    }

    private SpeciesDto getSpeciesResponse(SpeciesDto speciesDTO) {
        Species species = toEntity(speciesDTO);
        fillSpecies(speciesDTO, species);
        Species updatedSpecies = speciesRepository.save(species);
        return toDTO(updatedSpecies, false);
    }

    private void fillSpecies(SpeciesDto speciesDTO, Species species) {
        Optional.ofNullable(speciesDTO.getSubSpeciesUrls())
                .ifPresent(urls -> species.setSubSpecies(urls.stream().map(this::findByUrl).toList()));
    }

    private void saveSpeciesFeatures(CreateSpeciesDto createSpeciesDTO, Species species) {
        SpeciesFeatureConverter.convertDTOFeatureIntoEntityFeature(createSpeciesDTO.getFeatures(), species);
        Collection<SpeciesFeature> features = species.getFeatures();
        if (!CollectionUtils.isEmpty(features)) {
            features.stream()
                    .map(SpeciesFeature::getSource)
                    .filter(Objects::nonNull)
                    .forEach(this::validateAndSaveSource);
            speciesFeatureRepository.saveAll(features);
        }
    }
}

package club.ttg.dnd5.service.species;

import club.ttg.dnd5.dto.base.NameBasedDTO;
import club.ttg.dnd5.dto.base.create.SourceReference;
import club.ttg.dnd5.dto.species.CreateSpeciesDto;
import club.ttg.dnd5.dto.species.SpeciesCreateFeatureDto;
import club.ttg.dnd5.dto.species.SpeciesDto;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.model.base.Tag;
import club.ttg.dnd5.model.base.TagType;
import club.ttg.dnd5.model.book.Book;
import club.ttg.dnd5.model.book.Source;
import club.ttg.dnd5.model.species.Species;
import club.ttg.dnd5.model.species.SpeciesFeature;
import club.ttg.dnd5.repository.SpeciesRepository;
import club.ttg.dnd5.repository.TagRepository;
import club.ttg.dnd5.repository.book.BookRepository;
import club.ttg.dnd5.repository.book.SourceRepository;
import club.ttg.dnd5.utills.Converter;
import club.ttg.dnd5.utills.CreateConverter;
import club.ttg.dnd5.utills.SlugifyUtil;
import club.ttg.dnd5.utills.species.SpeciesFeatureConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static club.ttg.dnd5.utills.Converter.STRATEGY_SOURCE_CONSUMER;

@Service
@RequiredArgsConstructor
public class SpeciesService {
    private static final String BOOK_NOT_FOUND_FOR_URL = "Book not found for URL: ";
    private final SpeciesRepository speciesRepository;
    private final SourceRepository sourceRepository;
    private final BookRepository bookRepository;

    private final TagRepository tagRepository;
    // Public methods
    public SpeciesDto findById(String url) {
        return speciesRepository.findById(url)
                .map(species -> toDTO(species, false))
                .orElseThrow(() -> new EntityNotFoundException(url));
    }

    public boolean exists(String url) {
        return speciesRepository.existsById(url);
    }

    public List<SpeciesDto> getAllSpecies() {
        // только parent и убрать лишнюю детальную информацию
        return speciesRepository.findAllByParentIsNull()
                .stream()
                .map(species -> toDTO(species, true))
                .toList();
    }

    @Transactional
    public SpeciesDto save(CreateSpeciesDto createSpeciesDTO) {
        Species species = new Species();
        createSpeciesDTO.setLinkImageUrl(createSpeciesDTO.getLinkImageUrl());
        CreateConverter.MAP_BASE_DTO_TO_ENTITY_NAME.apply(createSpeciesDTO, species);
        Converter.MAP_CREATURE_PROPERTIES_DTO_TO_ENTITY.apply(createSpeciesDTO.getProperties(), species);
        SourceReference sourceDTO = createSpeciesDTO.getSourceDTO();
        if (createSpeciesDTO.getGallery() != null && !createSpeciesDTO.getGallery().isEmpty()) {
            species.setGalleryUrl(createSpeciesDTO.getGallery());
        }
        if (sourceDTO != null) {
            Book book = bookRepository.findByUrl(sourceDTO.getUrl())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, BOOK_NOT_FOUND_FOR_URL
                            + sourceDTO.getUrl()));
            Source source = new Source();
            source.setBookInfo(book);
            source.setPage(sourceDTO.getPage());
            species.setSource(source);
            validateSource(species.getSource());
        } else {
            //Тут хб, выступает игрок, а не какая-та книга
            Source source = new Source();
            source.setUserId(createSpeciesDTO.getUserId());
            species.setSource(source);
            sourceRepository.save(source);
        }
        collectTagsFromDTOtoEntity(createSpeciesDTO, species);
        collectCreateFeatureDTOtoEntity(createSpeciesDTO, species);
        handlingParentWhenCreateSpecies(species, createSpeciesDTO.getParent());
        Species save = speciesRepository.save(species);
        return toDTO(save, false);
    }

    private void handlingParentWhenCreateSpecies(Species species, String parentName) {
        if (parentName == null || parentName.isBlank()) {
            return;
        }

        if (species.getEnglish().equalsIgnoreCase(parentName) || species.getName().equalsIgnoreCase(parentName)) {
            // If the species is its own parent, save it as the parent
            speciesRepository.save(species); // Save to the database as a parent
            species.setParent(species); // Set itself as its parent
        } else {
            // Find the parent species by name in the database
            Species parent = speciesRepository.findByNameIgnoreCase(parentName)
                    .orElseThrow(() -> new IllegalArgumentException("Parent species not found: " + parentName));

            // Set the parent for the species
            species.setParent(parent);
        }
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

    private void validateSource(Source source) {
        if (source != null) {
            Book book = bookRepository.findById(source.getSourceAcronym())
                    .orElseThrow(() -> new EntityNotFoundException("Book not found with ID: " + source.getId()));
            source.setBookInfo(book);
        }
    }

    private Species toEntity(SpeciesDto dto) {
        Species species = new Species();
        species.setUrl(dto.getUrl());
        Converter.MAP_BASE_DTO_TO_ENTITY_NAME.apply(dto, species);
        Converter.MAP_DTO_SOURCE_TO_ENTITY_SOURCE.apply(dto.getSourceDTO(), species);
        Converter.MAP_CREATURE_PROPERTIES_DTO_TO_ENTITY.apply(dto.getCreatureProperties(), species);

        if (dto.getParent() != null) {
            String parentUrl = dto.getParent().getUrl();
            species.setParent(parentUrl.equals(dto.getUrl()) ? null : findByUrl(parentUrl));
        }

        fillSpecies(dto, species);
        if (!dto.getGallery().isEmpty()) {
            species.setGalleryUrl(dto.getGallery());
        }
        return species;
    }

    private SpeciesDto toDTO(Species species, boolean hideDetails) {
        SpeciesDto dto = new SpeciesDto();
        dto.setTags(species.getTags().stream().map(Tag::getName).collect(Collectors.toSet()));
        // Apply basic mapping (including base DTO and potentially hidden details)
        if (hideDetails) {
            Converter.MAP_ENTITY_TO_BASE_DTO.apply(dto, species);  // Base mapping for common properties
            Converter.MAP_ENTITY_SOURCE_TO_DTO_SOURCE.apply(dto.getSourceDTO(), species);
            Converter.MAP_ENTITY_TO_BASE_DTO_WITH_HIDE_DETAILS.apply(dto, species);
        } else {
            // Map base DTO and other properties
            Converter.MAP_ENTITY_TO_BASE_DTO.apply(dto, species);  // Base mapping for common properties
            Converter.MAP_ENTITY_SOURCE_TO_DTO_SOURCE.apply(dto.getSourceDTO(), species);  // Source mapping

            // Map CreatureProperties (movement and size related properties)
            Converter.MAP_ENTITY_TO_CREATURE_PROPERTIES_DTO.apply(dto.getCreatureProperties(), species);

            // Map parent and sub-species relationships
            handleParentAndChild(species, dto);

            // Map features (if any)
            Collection<SpeciesFeature> features = species.getFeatures();
            if (features != null) {
                dto.setFeatures(SpeciesFeatureConverter.convertEntityFeatureIntoDTOFeature(features));
            }
        }
        STRATEGY_SOURCE_CONSUMER.accept(dto, species.getSource());
        dto.setGallery(species.getGalleryUrl());
        return dto;
    }

    private void handleParentAndChild(Species species, SpeciesDto dto) {
        //parent
        Species speciesParent = species.getParent();
        if (speciesParent != null) {
            SpeciesDto parent = new SpeciesDto();
            // Set the URL
            parent.setUrl(speciesParent.getUrl());

            // Build the NameBasedDTO using a builder for better readability
            NameBasedDTO parentNameBased = NameBasedDTO.builder()
                    .name(speciesParent.getName())
                    .shortName(speciesParent.getShortName())
                    .english(speciesParent.getEnglish())
                    .build();

            // Set the NameBasedDTO in the parent
            parent.setNameBasedDTO(parentNameBased);
            dto.setParent(parent);
        }

        Collection<Species> speciesSubSpecies = species.getSubSpecies();
        if (speciesSubSpecies != null) {
            // Convert each sub-species to a LinkedSpeciesDto
            List<SpeciesDto> lineages = speciesSubSpecies.stream()
                    .map(subSpecies -> {
                        SpeciesDto linkedSpeciesDto = new SpeciesDto();

                        // Set the URL
                        linkedSpeciesDto.setUrl(subSpecies.getUrl());

                        // Build the NameBasedDTO
                        NameBasedDTO nameBasedDTO = NameBasedDTO.builder()
                                .name(subSpecies.getName())
                                .shortName(subSpecies.getShortName())
                                .english(subSpecies.getEnglish())
                                .build();

                        // Set the NameBasedDTO
                        linkedSpeciesDto.setNameBasedDTO(nameBasedDTO);

                        return linkedSpeciesDto;
                    })
                    .toList();

            // Set the list of LinkedSpeciesDto objects
            dto.setLineages(lineages);
        }
    }

    private SpeciesDto getSpeciesResponse(SpeciesDto speciesDTO) {
        Species species = toEntity(speciesDTO);
        fillSpecies(speciesDTO, species);
        Species updatedSpecies = speciesRepository.save(species);
        return toDTO(updatedSpecies, false);
    }

    private void fillSpecies(SpeciesDto speciesDTO, Species species) {
        Optional.ofNullable(speciesDTO.getLineages())
                .ifPresent(subSpeciesDtos -> species.setSubSpecies(
                        subSpeciesDtos.stream()
                                .map(this::convertToSpecies) // Convert LinkedSpeciesDto to Species
                                .toList()
                ));
    }

    private void collectTagsFromDTOtoEntity(CreateSpeciesDto createSpeciesDTO, Species species) {
        Set<String> tagNames = createSpeciesDTO.getTags(); // DTO returns tag names
        if (tagNames != null && !tagNames.isEmpty()) {
            Set<Tag> tags = tagNames.stream()
                    .map(tagName -> {
                        // Check if the tag exists in the database
                        Tag tag = tagRepository.findByNameIgnoreCase(tagName)
                                .orElseGet(() -> new Tag(tagName, TagType.TAG_SPECIES));
                        tag.getSpecies().add(species);
                        tagRepository.save(tag);
                        return tag;
                    })
                    .collect(Collectors.toSet());

            // Set the tags for the species
            species.setTags(tags);
        }
    }

    private void collectCreateFeatureDTOtoEntity(CreateSpeciesDto createSpeciesDto, Species species) {
        Collection<SpeciesCreateFeatureDto> features = createSpeciesDto.getFeatures();

        if (features != null && !features.isEmpty()) {
            Set<SpeciesFeature> speciesFeatures = new HashSet<>();

            for (SpeciesCreateFeatureDto featureDto : features) {
                SpeciesFeature speciesFeature = convertingSpeciesCreateFeatureToSpeciesFeature(featureDto, createSpeciesDto.getSourceDTO());
                speciesFeature.setUrl(createSpeciesDto.getUrl() + "/" + SlugifyUtil.getSlug(speciesFeature.getEnglish()));
                speciesFeatures.add(speciesFeature);
            }
            species.setFeatures(speciesFeatures);
        }
    }

    //советую обратить внимание на соурс, в случае, если у фичы она нулл, то берем соурс вида
    private SpeciesFeature convertingSpeciesCreateFeatureToSpeciesFeature(SpeciesCreateFeatureDto featureDto,
                                                                          SourceReference speciesSource) {
        SpeciesFeature speciesFeature = new SpeciesFeature();
        Source source = new Source();
        SourceReference featureSource = featureDto.getSource();
        if (featureSource != null) {
            Book book = bookRepository.findByUrl(featureSource.getUrl())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, BOOK_NOT_FOUND_FOR_URL
                            + featureSource.getUrl()));
            source.setBookInfo(book);
            source.setPage(featureSource.getPage());
        } else {
            Book book = bookRepository.findByUrl(speciesSource.getUrl())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, BOOK_NOT_FOUND_FOR_URL
                            + speciesSource.getUrl()));
            source.setBookInfo(book);
            source.setPage(speciesSource.getPage());
        }
        if (featureDto.getName() != null) {
            NameBasedDTO nameBasedDTO = featureDto.getName();
            speciesFeature.setName(nameBasedDTO.getName());
            speciesFeature.setShortName(nameBasedDTO.getShortName());
            speciesFeature.setEnglish(nameBasedDTO.getEnglish());
            speciesFeature.setAlternative(String.join(",", nameBasedDTO.getAlternative()));
        }
        speciesFeature.setDescription(featureDto.getDescription());
        speciesFeature.setSource(source);
        //хороший вопрос, может стоит сюда впихивать теги из вида, тип наследует теги вида
        speciesFeature.setTags(null);
        //вопрос ещё над imageUrl, стоит ли пихать сюда урл вида
        return speciesFeature;
    }

    /**
     * Converts a LinkedSpeciesDto to a Species entity.
     *
     * @param linkedSpeciesDto the LinkedSpeciesDto to convert
     * @return the corresponding Species entity
     */
    private Species convertToSpecies(SpeciesDto linkedSpeciesDto) {
        Species subSpecies = findByUrl(linkedSpeciesDto.getUrl()); // Find existing species by URL
        if (subSpecies == null) {
            throw new IllegalArgumentException("No species found for URL: " + linkedSpeciesDto.getUrl());
        }

        // Update additional fields if needed
        NameBasedDTO nameBasedDTO = linkedSpeciesDto.getNameBasedDTO();
        if (nameBasedDTO != null) {
            subSpecies.setName(nameBasedDTO.getName());
            subSpecies.setShortName(nameBasedDTO.getShortName());
            subSpecies.setEnglish(nameBasedDTO.getEnglish());
        }

        return subSpecies;
    }

    public boolean speciesExistsByUrl(String url) {
        Optional<Species> byId = speciesRepository.findById(url);
        return byId.isPresent();
    }
}

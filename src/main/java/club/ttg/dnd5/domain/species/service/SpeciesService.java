package club.ttg.dnd5.domain.species.service;

import club.ttg.dnd5.domain.common.rest.dto.NameResponse;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesShortResponse;
import club.ttg.dnd5.domain.species.rest.mapper.SpeciesMapper;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesRequest;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesDetailResponse;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.book.model.Source;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.repository.SpeciesRepository;
import club.ttg.dnd5.domain.common.repository.TagRepository;
import club.ttg.dnd5.domain.book.repository.BookRepository;
import club.ttg.dnd5.domain.book.SourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SpeciesService {
    private final SpeciesRepository speciesRepository;
    private final SourceRepository sourceRepository;
    private final BookRepository bookRepository;
    private final TagRepository tagRepository;
    private final SpeciesMapper speciesMapper;

    public boolean exists(String url) {
        return speciesRepository.existsById(url);
    }

    public SpeciesDetailResponse findById(String url) {
        return speciesRepository.findById(url)
                .map(speciesMapper::toDetailDto)
                .orElseThrow(() -> new EntityNotFoundException(url));
    }

    public List<SpeciesShortResponse> getSpecies() {
        return speciesRepository.findAllByParentIsNull()
                .stream()
                .map(speciesMapper::toShortDto)
                .toList();
    }

    @Transactional
    public SpeciesDetailResponse save(SpeciesRequest request) {
        Species species = speciesMapper.toEntity(request);
        var source = sourceRepository.findSourceByBookInfo_Url(request.getSource().getUrl())
                .orElseThrow();
        //species.setSource(source);
        //species.getFeatures().forEach(f -> f.setSource(source));
        if (speciesRepository.existsById(request.getUrl())) {
            throw new EntityExistException("Вид уже существует с URL: " + request.getUrl());
        }
        Species save = speciesRepository.save(species);
        return speciesMapper.toDetailDto(save);
    }

    public List<SpeciesDetailResponse> getLineages(String parentUrl) {
        return speciesRepository.findById(parentUrl)
                .filter(species -> !species.isHiddenEntity())
                .map(speciesRepository::findByParent)
                .orElseThrow(() -> new EntityNotFoundException("Parent species not found for URL: " + parentUrl))
                .stream()
                .map(speciesMapper::toDetailDto)
                .toList();
    }

    public List<SpeciesDetailResponse> getAllLineages(String subSpeciesUrl) {
        Species subSpecies = speciesRepository.findById(subSpeciesUrl)
                .orElseThrow(() -> new EntityNotFoundException("Sub-species not found for URL: " + subSpeciesUrl));

        return Stream.concat(
                        Stream.of(subSpecies),
                        Stream.concat(
                                Stream.ofNullable(subSpecies.getParent()),
                                subSpecies.getLineages() != null ? subSpecies.getLineages().stream() : Stream.empty()
                        )
                )
                .filter(species -> !species.isHiddenEntity())
                .map(speciesMapper::toDetailDto)
                .toList();
    }

    public SpeciesDetailResponse addParent(String speciesUrl, String speciesParentUrl) {
        Species species = findByUrl(speciesUrl);
        Species parent = findByUrl(speciesParentUrl);
        //на этапе save, мы делаем ссылку на самого себя, если это родитель
        //тут же мы проверяем это утверждение.
        if (parent.getParent().equals(parent)) {
            species.setParent(parent);

            Optional.ofNullable(parent.getLineages())
                    .orElseGet(() -> {
                        parent.setLineages(new ArrayList<>());
                        return parent.getLineages();
                    })
                    .add(species);

            return speciesMapper.toDetailDto(speciesRepository.save(species));
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "This is not a parent Species");
        }
    }

    @Transactional
    public SpeciesDetailResponse update(String oldUrl, SpeciesRequest request) {
        if (speciesRepository.existsById(oldUrl)) {
            if (!oldUrl.equals(request.getUrl())) {
                speciesRepository.deleteById(oldUrl);
            }
            return getSpeciesResponse(request);
        } else {
            throw new EntityNotFoundException("Species with URL " + oldUrl + " does not exist.");
        }
    }

    public SpeciesDetailResponse addSubSpecies(String speciesUrl, List<String> lineagesUrls) {
        Species species = findByUrl(speciesUrl);

        List<Species> subSpeciesEntities = lineagesUrls.stream()
                .map(url -> {
                    Species subSpecies = findByUrl(url);
                    subSpecies.setParent(species);
                    return subSpecies;
                })
                .toList();

        species.setLineages(subSpeciesEntities);
        return speciesMapper.toDetailDto(speciesRepository.save(species));
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

    private void handleParentAndChild(Species species, SpeciesDetailResponse dto) {
        //parent
        Species speciesParent = species.getParent();
        if (speciesParent != null) {
            SpeciesDetailResponse parent = new SpeciesDetailResponse();
            // Set the URL
            parent.setUrl(speciesParent.getUrl());

            // Build the NameBasedDTO using a builder for better readability
            NameResponse parentNameBased = NameResponse.builder()
                    .name(speciesParent.getName())
                    .english(speciesParent.getEnglish())
                    .build();

            // Set the NameBasedDTO in the parent
            parent.setName(parentNameBased);
            dto.setParent(parent);
        }

        Collection<Species> speciesSubSpecies = species.getLineages();
        if (speciesSubSpecies != null) {
            // Convert each sub-species to a LinkedSpeciesDto
            List<SpeciesDetailResponse> lineages = speciesSubSpecies.stream()
                    .map(subSpecies -> {
                        SpeciesDetailResponse linkedSpeciesDto = new SpeciesDetailResponse();

                        // Set the URL
                        linkedSpeciesDto.setUrl(subSpecies.getUrl());

                        // Build the NameBasedDTO
                        NameResponse nameBasedDTO = NameResponse.builder()
                                .name(subSpecies.getName())
                                .english(subSpecies.getEnglish())
                                .build();

                        // Set the NameBasedDTO
                        linkedSpeciesDto.setName(nameBasedDTO);

                        return linkedSpeciesDto;
                    })
                    .toList();

            // Set the list of LinkedSpeciesDto objects
            dto.setLineages(lineages);
        }
    }

    private SpeciesDetailResponse getSpeciesResponse(SpeciesRequest request) {
        Species species = speciesMapper.toEntity(request);
        Species updatedSpecies = speciesRepository.save(species);
        return speciesMapper.toDetailDto(updatedSpecies);
    }

    private void fillSpecies(SpeciesDetailResponse speciesDTO, Species species) {
        Optional.ofNullable(speciesDTO.getLineages())
                .ifPresent(subSpeciesDtos -> species.setLineages(
                        subSpeciesDtos.stream()
                                .map(this::convertToSpecies) // Convert LinkedSpeciesDto to Species
                                .toList()
                ));
    }


    /**
     * Converts a LinkedSpeciesDto to a Species entity.
     *
     * @param linkedSpeciesDto the LinkedSpeciesDto to convert
     * @return the corresponding Species entity
     */
    private Species convertToSpecies(SpeciesDetailResponse linkedSpeciesDto) {
        Species subSpecies = findByUrl(linkedSpeciesDto.getUrl()); // Find existing species by URL
        if (subSpecies == null) {
            throw new IllegalArgumentException("No species found for URL: " + linkedSpeciesDto.getUrl());
        }

        // Update additional fields if needed
        NameResponse nameBasedDTO = linkedSpeciesDto.getName();
        if (nameBasedDTO != null) {
            subSpecies.setName(nameBasedDTO.getName());
            subSpecies.setEnglish(nameBasedDTO.getEnglish());
        }
        return subSpecies;
    }
}

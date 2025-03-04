package club.ttg.dnd5.domain.species.service;

import club.ttg.dnd5.domain.species.rest.dto.SpeciesShortResponse;
import club.ttg.dnd5.domain.species.rest.mapper.SpeciesMapper;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesRequest;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesDetailResponse;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.repository.SpeciesRepository;
import club.ttg.dnd5.domain.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SpeciesService {
    private final SpeciesRepository speciesRepository;
    private final BookRepository bookRepository;
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
        if (speciesRepository.existsById(request.getUrl())) {
            throw new EntityExistException("Вид уже существует с URL: " + request.getUrl());
        }
        Species species = speciesMapper.toEntity(request);
        if (StringUtils.hasText(request.getParentUrl())) {
            var parent = findByUrl(request.getParentUrl());
            species.setParent(parent);
        }
        var book = bookRepository.findByUrl(request.getSource().getUrl())
                .orElseThrow(() -> new EntityNotFoundException("Книга не найдена: "
                        + request.getSource().getUrl()));

        species.setSource(book);

        Species save = speciesRepository.save(species);
        return speciesMapper.toDetailDto(save);
    }

    public List<SpeciesDetailResponse> getLineages(String parentUrl) {
        return speciesRepository.findById(parentUrl)
                .filter(species -> !species.isHiddenEntity())
                .map(speciesRepository::findByParent)
                .orElseThrow(() -> new EntityNotFoundException("Вид не найден для URL: " + parentUrl))
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

    private SpeciesDetailResponse getSpeciesResponse(SpeciesRequest request) {
        Species species = speciesMapper.toEntity(request);
        Species updatedSpecies = speciesRepository.save(species);
        return speciesMapper.toDetailDto(updatedSpecies);
    }
}

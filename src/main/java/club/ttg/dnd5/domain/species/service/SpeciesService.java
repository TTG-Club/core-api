package club.ttg.dnd5.domain.species.service;

import club.ttg.dnd5.domain.book.service.BookService;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.repository.SpeciesRepository;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesDetailResponse;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesRequest;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesShortResponse;
import club.ttg.dnd5.domain.species.rest.mapper.SpeciesMapper;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpeciesService {
    private final SpeciesRepository speciesRepository;
    private final BookService bookService;
    private final SpeciesMapper speciesMapper;

    public boolean exists(String url) {
        return speciesRepository.existsById(url);
    }

    public SpeciesDetailResponse findById(String url) {
        var species = speciesRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(url));
        if (species.getParent() != null) {
            if (species.getFeatures() != null) {
                species.getFeatures().addAll(species.getParent().getFeatures());
            } else {
                species.setFeatures(species.getParent().getFeatures());
            }
        }
        return speciesMapper.toDetailDto(species);
    }

    public List<Species> findAllById(Collection<String> urls) {
        return speciesRepository.findAllById(urls);
    }

    public List<SpeciesShortResponse> getSpecies(String searchLine, String[] sort) {
        Collection<Species> specieses;
        if (StringUtils.hasText(searchLine)) {
            String invertedSearchLine = SwitchLayoutUtils.switchLayout(searchLine);
            specieses =  speciesRepository.findAllSearch(searchLine, invertedSearchLine, Sort.by(sort));
        } else {
            specieses = speciesRepository.findAllByParentIsNull(Sort.by(sort));
        }
        return specieses.stream()
                .map(speciesMapper::toShortDto)
                .toList();
    }

    @Transactional
    @CacheEvict(cacheNames = "countAllMaterials")
    public SpeciesDetailResponse save(SpeciesRequest request) {
        if (speciesRepository.existsById(request.getUrl())) {
            throw new EntityExistException("Вид уже существует с URL: " + request.getUrl());
        }
        return saveSpecies(request);
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

    public Collection<SpeciesShortResponse> getAllLineages(String url) {
        Species species = speciesRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Вид не найден URL: " + url));
        return species.getLineages().stream()
            .map(speciesMapper::toShortDto)
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
            return saveSpecies(request);
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

    public SpeciesRequest findFormByUrl(final String url) {
        return speciesMapper.toRequest(findByUrl(url));
    }

    // Private methods
    private Species findByUrl(String url) {
        return speciesRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Species not found with URL: " + url));
    }

    private SpeciesDetailResponse saveSpecies(SpeciesRequest request) {
        Species species = speciesMapper.toEntity(request);
        if (StringUtils.hasText(request.getParent())) {
            var parent = findByUrl(request.getParent());
            species.setParent(parent);
        }
        var book = bookService.findByUrl(request.getSource().getUrl());

        species.setSource(book);

        Species save = speciesRepository.save(species);
        return speciesMapper.toDetailDto(save);
    }

}

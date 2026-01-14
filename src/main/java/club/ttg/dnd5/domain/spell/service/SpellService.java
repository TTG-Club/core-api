package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.service.SourceService;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.service.ClassService;
import club.ttg.dnd5.domain.common.rest.dto.SourceRequest;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.service.SpeciesService;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.spell.rest.dto.SpellDetailedResponse;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.domain.spell.rest.dto.create.CreateAffiliationRequest;
import club.ttg.dnd5.domain.spell.rest.dto.create.SpellRequest;
import club.ttg.dnd5.domain.spell.rest.mapper.SpellMapper;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.util.UrlParameterConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpellService {
    private final ClassService classService;
    private final SpeciesService speciesService;
    private final SourceService sourceService;
    private final SpellRepository spellRepository;
    private final SpellMapper spellMapper;
    private final SpellQueryDslSearchService spellQueryDslSearchService;

    private final ObjectMapper objectMapper;

    public boolean existOrThrow(String url) {
        if (!spellRepository.existsById(url)) {
            throw new EntityNotFoundException(String.format("Заклинание с url %s не существует", url));
        }
        return true;
    }

    public List<SpellShortResponse> search(final @Valid @Size(min = 2) String searchLine,
                                           final String filter) {
        SearchBody searchBody = parseFilter(filter);
        return spellQueryDslSearchService.search(searchLine, searchBody)
                .stream()
                .map(spellMapper::toShort)
                .collect(Collectors.toList());
    }

    public List<SpellShortResponse> search(String searchLine, SearchBody searchBody) {
        return spellQueryDslSearchService.search(searchLine, searchBody)
                .stream()
                .map(spellMapper::toShort)
                .collect(Collectors.toList());
    }

    public SpellDetailedResponse findDetailedByUrl(String url) {
        return spellMapper.toDetail(findByUrl(url));
    }

    public SpellRequest findFormByUrl(final String url) {
        return spellMapper.toRequest(findByUrl(url));
    }

    public Spell findByUrl(String url) {
        return spellRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Заклинание с url %s не существует", url)));
    }

    public boolean existsByUrl(String url) {
        return spellRepository.existsById(url);
    }

    @Transactional
    @CacheEvict(cacheNames = "countAllMaterials")
    public String save(SpellRequest request) {
        if (existsByUrl(request.getUrl())) {
            throw new EntityExistException(String.format("Заклинание с url %s уже существует", request.getUrl()));
        }
        List<CharacterClass> classes = getClasses(request);
        List<CharacterClass> subclasses = getSubclasses(request);
        List<Species> species = getSpecieses(request);
        List<Species> lineages = getLineages(request);

        Source source = Optional.ofNullable(request.getSource())
                .map(SourceRequest::getUrl)
                .map(sourceService::findByUrl)
                .orElse(null);

        Spell spell = spellMapper.toEntity(request, source, classes, subclasses, species, lineages);
        spell.setUpcastable(spell.getLevel() > 0 && StringUtils.hasText(spell.getUpper()));
        return spellMapper.toDetail(spellRepository.save(spell)).getUrl();

    }

    private List<CharacterClass> getClasses(SpellRequest request) {
        return Optional.ofNullable(request.getAffiliations())
                .map(CreateAffiliationRequest::getClasses)
                .map(classService::findAllById)
                .orElseGet(Collections::emptyList);
    }

    private List<CharacterClass> getSubclasses(SpellRequest request) {
        return Optional.ofNullable(request.getAffiliations())
                .map(CreateAffiliationRequest::getSubclasses)
                .map(classService::findAllById)
                .orElseGet(Collections::emptyList);
    }

    private List<Species> getSpecieses(SpellRequest request) {
        return Optional.ofNullable(request.getAffiliations())
                .map(CreateAffiliationRequest::getSpecies)
                .map(speciesService::findAllById)
                .orElseGet(Collections::emptyList);
    }

    private List<Species> getLineages(SpellRequest request) {
        return Optional.ofNullable(request.getAffiliations())
                .map(CreateAffiliationRequest::getLineages)
                .map(speciesService::findAllById)
                .orElseGet(Collections::emptyList);
    }

    @Transactional
    public String update(String oldUrl, @Valid SpellRequest request) {
        Spell existingSpell = findByUrl(oldUrl);
        List<Species> species = getSpecieses(request);
        List<Species> lineages = getLineages(request);
        List<CharacterClass> classes = getClasses(request);
        List<CharacterClass> subclasses = getSubclasses(request);

        Source source = Optional.ofNullable(request.getSource())
                .map(SourceRequest::getUrl)
                .map(sourceService::findByUrl)
                .orElse(null);
        spellMapper.updateEntity(existingSpell, request);

        existingSpell.setSource(source);
        existingSpell.setSpeciesAffiliation(species);
        existingSpell.setLineagesAffiliation(lineages);
        existingSpell.setClassAffiliation(classes);
        existingSpell.setSubclassAffiliation(subclasses);

        existingSpell.setUpcastable(existingSpell.getLevel() > 0 && StringUtils.hasText(existingSpell.getUpper()));
        return existingSpell.getUrl();
    }

    @Transactional
    @CacheEvict(cacheNames = "countAllMaterials")
    public void delete(String url) {
        Spell existingSpell = findByUrl(url);
        existingSpell.setHiddenEntity(true);
        spellRepository.save(existingSpell);
    }

    public SpellDetailedResponse preview(final SpellRequest request) {
        var book = sourceService.findByUrl(request.getSource().getUrl());
        List<CharacterClass> classes = getClasses(request);
        List<CharacterClass> subclasses = getSubclasses(request);
        List<Species> species = getSpecieses(request);
        List<Species> lineages = getLineages(request);
        return spellMapper.toDetail(
                spellMapper.toEntity(request, book, classes, subclasses, species, lineages)
        );
    }

    private SearchBody parseFilter(final String filter)
    {
        if (!StringUtils.hasText(filter))
        {
            return new SearchBody(new FilterInfo());
        }

        final String json;
        try
        {
            json = UrlParameterConverter.decompression(filter);
        }
        catch (Exception e)
        {
            return new SearchBody(new FilterInfo());
        }

        if (!StringUtils.hasText(json))
        {
            return new SearchBody(new FilterInfo());
        }

        try
        {
            FilterInfo filterInfo = objectMapper.readValue(json, FilterInfo.class);
            return new SearchBody(filterInfo);
        }
        catch (JsonProcessingException e)
        {
            return new SearchBody(new FilterInfo());
        }
    }
}

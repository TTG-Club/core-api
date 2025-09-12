package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.book.service.BookService;
import club.ttg.dnd5.domain.common.rest.dto.PageResponse;
import club.ttg.dnd5.domain.common.rest.dto.Pagination;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpellService {
    private final SpeciesService speciesService;
    private final BookService bookService;
    private final SpellRepository spellRepository;
    private final SpellMapper spellMapper;
    private final SpellQueryDslSearchService spellDslSearchService;

    public boolean existOrThrow(String url) {
        if (!spellRepository.existsById(url)) {
            throw new EntityNotFoundException(String.format("Заклинание с url %s не существует", url));
        }
        return true;
    }

    public PageResponse<SpellShortResponse> search(String searchLine,
                                                   final int page,
                                                   final int limit,
                                                   final String[] sort,
                                                   SearchBody searchBody) {
        var responseItems = spellDslSearchService.search(
                        searchLine, page, limit, sort, searchBody)
                .stream()
                .map(spellMapper::toShort)
                .toList();
        var pagination = Pagination.of(page,
                limit,
                spellRepository.count(),
                spellDslSearchService.count(searchLine, searchBody)
        );
        return PageResponse.<SpellShortResponse>builder()
                .items(responseItems)
                .pagination(pagination)
                .build();
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
        List<Species> species = getSpecieses(request);
        List<Species> lineages = getLineages(request);
        //TODO долить связи с классами и происхождениями

        Book book = Optional.ofNullable(request.getSource())
                .map(SourceRequest::getUrl)
                .map(bookService::findByUrl)
                .orElse(null);

        Spell spell = spellMapper.toEntity(request, book, Collections.emptyList(), Collections.emptyList(), species, lineages);
        spell.setUpcastable(spell.getLevel() > 0 && StringUtils.hasText(spell.getUpper()));
        return spellMapper.toDetail(spellRepository.save(spell)).getUrl();

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
        //TODO долить связи с классами и происхождениями

        Book book = Optional.ofNullable(request.getSource())
                .map(SourceRequest::getUrl)
                .map(bookService::findByUrl)
                .orElse(null);
        Spell spell = spellMapper.updateEntity(existingSpell, request, book, Collections.emptyList(), Collections.emptyList(), species, lineages);
        spell.setUpcastable(spell.getLevel() > 0 && StringUtils.hasText(spell.getUpper()));
        if (!Objects.equals(oldUrl, request.getUrl())) {
            spellRepository.deleteById(oldUrl);
            spellRepository.flush();
        }
        return spellMapper.toDetail(spellRepository.save(spell)).getUrl();
    }

    @Transactional
    @CacheEvict(cacheNames = "countAllMaterials")
    public void delete(String url) {
        Spell existingSpell = findByUrl(url);
        existingSpell.setHiddenEntity(true);
        spellRepository.save(existingSpell);
    }

    public SpellDetailedResponse preview(final SpellRequest request) {
        var book = bookService.findByUrl(request.getSource().getUrl());
        List<Species> species = getSpecieses(request);
        List<Species> lineages = getLineages(request);
        return spellMapper.toDetail(
                spellMapper.toEntity(request, book, Collections.emptyList(), Collections.emptyList(), species, lineages)
        );
    }
}

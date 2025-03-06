package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.book.service.BookService;
import club.ttg.dnd5.domain.common.rest.dto.SourceRequest;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.service.SpeciesService;
import club.ttg.dnd5.domain.spell.mapper.SpellMapper;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.spell.rest.dto.SpellDetailedResponse;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.domain.spell.rest.dto.create.CreateAffiliationRequest;
import club.ttg.dnd5.domain.spell.rest.dto.create.SpellRequest;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpellService {
    private final SpeciesService speciesService;
    private final BookService bookService;

    private final SpellRepository spellRepository;

    private final SpellMapper spellMapper;

    public boolean existOrThrow(String url) {
        if (!spellRepository.existsById(url)) {
            throw new EntityNotFoundException(String.format("Заклинание с url %s не существует", url));
        }
        return true;
    }

    public List<SpellShortResponse> search(String searchLine) {
        return Optional.ofNullable(searchLine)
                .filter(StringUtils::isNotBlank)
                .map(line -> {
                    String invertedSearchLine = SwitchLayoutUtils.switchLayout(line);
                    return spellRepository.findBySearchLine(line, invertedSearchLine);
                })
                .orElseGet(this::findAll)
                .stream()
                .sorted(Comparator.comparing(Spell::getLevel, Comparator.naturalOrder()).thenComparing(Spell::getName, Comparator.naturalOrder()))
                .map(spellMapper::toSpeciesShortResponse)
                .collect(Collectors.toList());
    }

    public List<Spell> findAll() {
        return spellRepository.findAll();
    }

    public SpellDetailedResponse findDetailedByUrl(String url) {
        return spellMapper.toSpellDetailedResponse(findByUrl(url));
    }

    public Spell findByUrl(String url) {
        return spellRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Заклинание с url %s не существует", url)));
    }

    public boolean existsByUrl(String url) {
        return spellRepository.existsById(url);
    }

    @Transactional
    public SpellDetailedResponse save(SpellRequest request) {
        if (existsByUrl(request.getUrl())) {
            throw new EntityExistException(String.format("Заклинание с url %s уже существует", request.getUrl()));
        }
        List<Species> species = Optional.ofNullable(request.getAffiliations())
                .map(CreateAffiliationRequest::getSpecies)
                .map(speciesService::findAllById)
                .orElseGet(Collections::emptyList);
        List<Species> lineages = Optional.ofNullable(request.getAffiliations())
                .map(CreateAffiliationRequest::getLineages)
                .map(speciesService::findAllById)
                .orElseGet(Collections::emptyList);
        //TODO долить связи с классами и происхождениями

        Book book = Optional.ofNullable(request.getSource())
                .map(SourceRequest::getUrl)
                .map(bookService::findByUrl)
                .orElse(null);

        Spell spell = spellMapper.toEntity(request, book, Collections.emptyList(), Collections.emptyList(), species, lineages);

        return spellMapper.toSpellDetailedResponse(spellRepository.save(spell));

    }

    @Transactional
    public SpellDetailedResponse update(String oldUrl, @Valid SpellRequest request) {
        Spell existingSpell = findByUrl(oldUrl);
        List<Species> species = Optional.ofNullable(request.getAffiliations())
                .map(CreateAffiliationRequest::getSpecies)
                .map(speciesService::findAllById)
                .orElseGet(Collections::emptyList);
        List<Species> lineages = Optional.ofNullable(request.getAffiliations())
                .map(CreateAffiliationRequest::getLineages)
                .map(speciesService::findAllById)
                .orElseGet(Collections::emptyList);
        //TODO долить связи с классами и происхождениями

        Book book = Optional.ofNullable(request.getSource())
                .map(SourceRequest::getUrl)
                .map(bookService::findByUrl)
                .orElse(null);
        Spell spell = spellMapper.updateEntity(existingSpell, request, book, Collections.emptyList(), Collections.emptyList(), species, lineages);
        return spellMapper.toSpellDetailedResponse(spellRepository.save(spell));
    }

    @Transactional
    public void delete(String url) {
        Spell existingSpell = findByUrl(url);
        existingSpell.setHiddenEntity(true);
        spellRepository.save(existingSpell);
    }
}

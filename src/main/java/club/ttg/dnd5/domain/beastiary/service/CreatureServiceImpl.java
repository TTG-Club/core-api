package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.repository.CreatureRepository;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureShortResponse;
import club.ttg.dnd5.domain.beastiary.rest.mapper.CreatureMapper;
import club.ttg.dnd5.domain.book.service.BookService;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CreatureServiceImpl implements CreatureService {
    private static final Sort DEFAULT_SORT = Sort.by("experience", "name");
    private final CreatureRepository creatureRepository;
    private final BookService bookService;
    private final CreatureMapper creatureMapper;

    @Override
    public Boolean existOrThrow(final String url) {
        if (!creatureRepository.existsById(url)) {
            throw new EntityNotFoundException(String.format("Существо с url %s не существует", url));
        }
        return true;
    }

    @Override
    public List<CreatureShortResponse> search(final String searchLine) {
        return Optional.ofNullable(searchLine)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(line -> {
                    String invertedSearchLine = SwitchLayoutUtils.switchLayout(line);
                    return creatureRepository.findBySearchLine(line, invertedSearchLine, DEFAULT_SORT);
                })
                .orElseGet(() -> creatureRepository.findAll(DEFAULT_SORT))
                .stream()
                .map(creatureMapper::toShort)
                .collect(Collectors.toList());
    }

    @Override
    public CreatureDetailResponse findDetailedByUrl(final String url) {
        return creatureMapper.toDetail(findByUrl(url));
    }

    @Override
    public CreatureRequest findFormByUrl(final String url) {
        return creatureMapper.toRequest(findByUrl(url));
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    public String save(final CreatureRequest request) {
        if (creatureRepository.existsById(request.getUrl())) {
            throw new EntityExistException("Существо уже существует с URL: " + request.getUrl());
        }
        var book = bookService.findByUrl(request.getSource().getUrl());
        var beast = creatureMapper.toEntity(request, book);
        return creatureRepository.save(beast).getUrl();
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    public String update(final String url, final CreatureRequest request) {
        findByUrl(url);
        if (!url.equalsIgnoreCase(request.getUrl())) {
            creatureRepository.deleteById(url);
        }
        var book = bookService.findByUrl(request.getSource().getUrl());
        var beast = creatureMapper.toEntity(request, book);
        return creatureRepository.save(beast).getUrl();
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    public String delete(final String url) {
        Creature existing = findByUrl(url);
        existing.setHiddenEntity(true);
        return creatureRepository.save(existing).getUrl();
    }

    @Override
    public CreatureDetailResponse preview(final CreatureRequest request) {
        var book = bookService.findByUrl(request.getSource().getUrl());
        return creatureMapper.toDetail(creatureMapper.toEntity(request, book));
    }

    private Creature findByUrl(String url) {
        return creatureRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Существо с URL: %s не существует", url)));
    }
}

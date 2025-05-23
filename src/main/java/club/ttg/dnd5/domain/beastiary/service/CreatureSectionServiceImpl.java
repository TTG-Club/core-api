package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.model.section.CreatureSection;
import club.ttg.dnd5.domain.beastiary.repository.CreatureSectionRepository;
import club.ttg.dnd5.domain.beastiary.rest.dto.section.CreatureSectionDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.section.CretureSectionRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.section.CreatureSectionShortResponse;
import club.ttg.dnd5.domain.beastiary.rest.mapper.CreatureSectionMapper;
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
public class CreatureSectionServiceImpl implements CreatureSectionService {
    private static final Sort DEFAULT_SORT = Sort.by("experience", "name");
    private final CreatureSectionRepository creatureSectionRepository;
    private final BookService bookService;
    private final CreatureSectionMapper creatureSectionMapper;

    @Override
    public Boolean existOrThrow(final String url) {
        if (!creatureSectionRepository.existsById(url)) {
            throw new EntityNotFoundException(String.format("Существо с url %s не существует", url));
        }
        return true;
    }

    @Override
    public List<CreatureSectionShortResponse> search(final String searchLine) {
        return Optional.ofNullable(searchLine)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(line -> {
                    String invertedSearchLine = SwitchLayoutUtils.switchLayout(line);
                    return creatureSectionRepository.findBySearchLine(line, invertedSearchLine, DEFAULT_SORT);
                })
                .orElseGet(() -> creatureSectionRepository.findAll(DEFAULT_SORT))
                .stream()
                .map(creatureSectionMapper::toShort)
                .collect(Collectors.toList());
    }

    @Override
    public CreatureSectionDetailResponse findDetailedByUrl(final String url) {
        return creatureSectionMapper.toDetail(findByUrl(url));
    }

    @Override
    public CretureSectionRequest findFormByUrl(final String url) {
        return creatureSectionMapper.toRequest(findByUrl(url));
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    public String save(final CretureSectionRequest request) {
        if (creatureSectionRepository.existsById(request.getUrl())) {
            throw new EntityExistException("Существо уже существует с URL: " + request.getUrl());
        }
        var book = bookService.findByUrl(request.getSource().getUrl());
        var beast = creatureSectionMapper.toEntity(request, book);
        return creatureSectionRepository.save(beast).getUrl();
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    public String update(final String url, final CretureSectionRequest request) {
        findByUrl(url);
        if (!url.equalsIgnoreCase(request.getUrl())) {
            creatureSectionRepository.deleteById(url);
        }
        var book = bookService.findByUrl(request.getSource().getUrl());
        var beast = creatureSectionMapper.toEntity(request, book);
        return creatureSectionRepository.save(beast).getUrl();
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    public String delete(final String url) {
        CreatureSection existing = findByUrl(url);
        existing.setHiddenEntity(true);
        return creatureSectionRepository.save(existing).getUrl();
    }

    private CreatureSection findByUrl(String url) {
        return creatureSectionRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Существо с URL: %s не существует", url)));
    }
}

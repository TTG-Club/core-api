package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.model.Beast;
import club.ttg.dnd5.domain.beastiary.model.section.BeastSection;
import club.ttg.dnd5.domain.beastiary.repository.BeastSectionRepository;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.section.BeastSectionDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.section.BeastSectionRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.section.BeastSectionShortResponse;
import club.ttg.dnd5.domain.beastiary.rest.mapper.BeastSectionMapper;
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
public class BeastSectionServiceImpl implements BeastSectionService {
    private static final Sort DEFAULT_SORT = Sort.by("experience", "name");
    private final BeastSectionRepository beastSectionRepository;
    private final BookService bookService;
    private final BeastSectionMapper beastSectionMapper;

    @Override
    public Boolean existOrThrow(final String url) {
        if (!beastSectionRepository.existsById(url)) {
            throw new EntityNotFoundException(String.format("Существо с url %s не существует", url));
        }
        return true;
    }

    @Override
    public List<BeastSectionShortResponse> search(final String searchLine) {
        return Optional.ofNullable(searchLine)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(line -> {
                    String invertedSearchLine = SwitchLayoutUtils.switchLayout(line);
                    return beastSectionRepository.findBySearchLine(line, invertedSearchLine, DEFAULT_SORT);
                })
                .orElseGet(() -> beastSectionRepository.findAll(DEFAULT_SORT))
                .stream()
                .map(beastSectionMapper::toShort)
                .collect(Collectors.toList());
    }

    @Override
    public BeastSectionDetailResponse findDetailedByUrl(final String url) {
        return beastSectionMapper.toDetail(findByUrl(url));
    }

    @Override
    public BeastSectionRequest findFormByUrl(final String url) {
        return beastSectionMapper.toRequest(findByUrl(url));
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    public String save(final BeastSectionRequest request) {
        if (beastSectionRepository.existsById(request.getUrl())) {
            throw new EntityExistException("Существо уже существует с URL: " + request.getUrl());
        }
        var book = bookService.findByUrl(request.getSource().getUrl());
        var beast = beastSectionMapper.toEntity(request, book);
        return beastSectionRepository.save(beast).getUrl();
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    public String update(final String url, final BeastSectionRequest request) {
        findByUrl(url);
        if (!url.equalsIgnoreCase(request.getUrl())) {
            beastSectionRepository.deleteById(url);
        }
        var book = bookService.findByUrl(request.getSource().getUrl());
        var beast = beastSectionMapper.toEntity(request, book);
        return beastSectionRepository.save(beast).getUrl();
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    public String delete(final String url) {
        BeastSection existing = findByUrl(url);
        existing.setHiddenEntity(true);
        return beastSectionRepository.save(existing).getUrl();
    }

    private BeastSection findByUrl(String url) {
        return beastSectionRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Существо с URL: %s не существует", url)));
    }
}

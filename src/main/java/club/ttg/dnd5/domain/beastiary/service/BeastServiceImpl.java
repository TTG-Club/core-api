package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.model.Beast;
import club.ttg.dnd5.domain.beastiary.repository.BeastRepository;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastShortResponse;
import club.ttg.dnd5.domain.beastiary.rest.mapper.BeastMapper;
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
public class BeastServiceImpl implements BeastService {
    private static final Sort DEFAULT_SORT = Sort.by("level", "name");
    private final BeastRepository beastRepository;
    private final BookService bookService;
    private final BeastMapper beastMapper;

    @Override
    public Boolean existOrThrow(final String url) {
        if (!beastRepository.existsById(url)) {
            throw new EntityNotFoundException(String.format("Существо с url %s не существует", url));
        }
        return true;
    }

    @Override
    public List<BeastShortResponse> search(final String searchLine) {
        return Optional.ofNullable(searchLine)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(line -> {
                    String invertedSearchLine = SwitchLayoutUtils.switchLayout(line);
                    return beastRepository.findBySearchLine(line, invertedSearchLine, DEFAULT_SORT);
                })
                .orElseGet(() -> beastRepository.findAll(DEFAULT_SORT))
                .stream()
                .map(beastMapper::toShort)
                .collect(Collectors.toList());
    }

    @Override
    public BeastDetailResponse findDetailedByUrl(final String url) {
        return beastMapper.toDetail(findByUrl(url));
    }

    @Override
    public BeastRequest findFormByUrl(final String url) {
        return beastMapper.toRequest(findByUrl(url));
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    public String save(final BeastRequest request) {
        if (beastRepository.existsById(request.getUrl())) {
            throw new EntityExistException("Существо уже существует с URL: " + request.getUrl());
        }
        var book = bookService.findByUrl(request.getSource().getUrl());
        var beast = beastMapper.toEntity(request, book);
        return beastRepository.save(beast).getUrl();
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    public String update(final String url, final BeastRequest request) {
        findByUrl(url);
        if (!url.equalsIgnoreCase(request.getUrl())) {
            beastRepository.deleteById(url);
        }
        var book = bookService.findByUrl(request.getSource().getUrl());
        var beast = beastMapper.toEntity(request, book);
        return beastRepository.save(beast).getUrl();
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    public String delete(final String url) {
        Beast existing = findByUrl(url);
        existing.setHiddenEntity(true);
        return beastRepository.save(existing).getUrl();
    }

    private Beast findByUrl(String url) {
        return beastRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Существо с URL: %s не существует", url)));
    }
}

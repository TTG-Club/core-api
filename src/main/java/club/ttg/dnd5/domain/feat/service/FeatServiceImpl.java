package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.book.service.BookService;
import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;
import club.ttg.dnd5.domain.feat.rest.dto.FeatShortResponse;
import club.ttg.dnd5.domain.feat.rest.mapper.FeatMapper;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FeatServiceImpl implements FeatService {
    private static final Sort DEFAULT_SORT = Sort.by("category", "name");
    private final FeatRepository featRepository;
    private final BookService bookService;
    private final FeatMapper featMapper;

    @Override
    public FeatDetailResponse getFeat(final String featUrl) {
        return featMapper.toDetailDto(findByUrl(featUrl));
    }

    @Override
    public Collection<FeatShortResponse> getFeats(final @Valid @Size String searchLine) {
        return Optional.ofNullable(searchLine)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(line -> {
                    String invertedSearchLine = SwitchLayoutUtils.switchLayout(line);
                    return featRepository.findBySearchLine(line, invertedSearchLine, DEFAULT_SORT);
                })
                .orElseGet(() -> featRepository.findAll(DEFAULT_SORT))
                .stream()
                .map(featMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    @CacheEvict(cacheNames = "countAllMaterials")
    public String addFeat(final FeatRequest dto) {
        if (featRepository.existsById(dto.getUrl())) {
            throw new EntityExistException("Feat exist by URL: " + dto.getUrl());
        }
        var book = bookService.findByUrl(dto.getSource().getUrl());
        var feat = featMapper.toEntity(dto, book);
        return featRepository.save(feat).getUrl();
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    public String updateFeat(final String featUrl, final FeatRequest dto) {
        var entity = findByUrl(featUrl);
        var book = bookService.findByUrl(dto.getSource().getUrl());
        var feat = featMapper.toEntity(dto, book);
        if (!Objects.equals(featUrl, dto.getUrl())) {
            featRepository.deleteById(featUrl);
            featRepository.flush();
        }
        return featRepository.save(feat).getUrl();
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    @CacheEvict(cacheNames = "countAllMaterials")
    public String delete(final String featUrl) {
        var entity = findByUrl(featUrl);
        entity.setHiddenEntity(true);
        return featRepository.save(entity).getUrl();
    }

    @Override
    public boolean existOrThrow(final String url) {
        if (!featRepository.existsById(url)) {
            throw new EntityNotFoundException(String.format("Черта с url %s не существует", url));
        }
        return true;
    }

    @Override
    public FeatRequest findFormByUrl(final String url) {
        return featMapper.toRequest(findByUrl(url));
    }

    private Feat findByUrl(String url) {
        return featRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Черта не найдена по URL: " + url));
    }
}

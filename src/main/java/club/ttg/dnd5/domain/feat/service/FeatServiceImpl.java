package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.book.service.BookService;
import club.ttg.dnd5.domain.common.rest.dto.PageResponse;
import club.ttg.dnd5.domain.common.rest.dto.Pagination;
import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;
import club.ttg.dnd5.domain.feat.rest.dto.FeatShortResponse;
import club.ttg.dnd5.domain.feat.rest.mapper.FeatMapper;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FeatServiceImpl implements FeatService {
    private final FeatQueryDslSearchService featQueryDslSearchService;
    private final FeatRepository featRepository;
    private final BookService bookService;
    private final FeatMapper featMapper;

    @Override
    public FeatDetailResponse getFeat(final String featUrl) {
        return featMapper.toDetail(findByUrl(featUrl));
    }

    @Override
    public PageResponse<FeatShortResponse> getFeats(final @Valid @Size String searchLine,
                                                    final int page,
                                                    final int limit,
                                                    final String sort, final SearchBody searchBody) {
        var responseItems = featQueryDslSearchService.search(
                        searchLine, page, limit, sort, searchBody)
                .stream()
                .map(featMapper::toShort)
                .toList();
        var pagination = Pagination.of(page, limit, featRepository.count(), responseItems.size());
        return PageResponse.<FeatShortResponse>builder()
                .items(responseItems)
                .pagination(pagination)
                .build();
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
        findByUrl(featUrl);
        if (!featUrl.equalsIgnoreCase(dto.getUrl())) {
            featRepository.deleteById(featUrl);
        }
        var book = bookService.findByUrl(dto.getSource().getUrl());
        var feat = featMapper.toEntity(dto, book);
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

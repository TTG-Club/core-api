package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.background.repository.BackgroundRepository;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundDetailResponse;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundRequest;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundShortResponse;
import club.ttg.dnd5.domain.background.rest.mapper.BackgroundMapper;
import club.ttg.dnd5.domain.book.service.BookService;
import club.ttg.dnd5.domain.common.rest.dto.PageResponse;
import club.ttg.dnd5.domain.common.rest.dto.Pagination;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BackgroundServiceImpl implements BackgroundService {
    private final BackgroundRepository backgroundRepository;
    private final BackgroundQDslSearchService backgroundSearchService;
    private final FeatRepository featRepository;
    private final BookService bookService;
    private final BackgroundMapper backgroundMapper;

    @Override
    public BackgroundDetailResponse getBackground(final String backgroundUrl) {
        return backgroundMapper.toDetail(findByUrl(backgroundUrl));
    }

    @Override
    public PageResponse<BackgroundShortResponse> getBackgrounds(String searchLine,
                                                                final int page,
                                                                final int limit,
                                                                final String sort, final SearchBody searchBody) {
        var responseItems = backgroundSearchService.search(
                        searchLine, page, limit, sort, searchBody)
                .stream()
                .map(backgroundMapper::toShort)
                .toList();
        var pagination = Pagination.of(page, limit, backgroundRepository.count(), responseItems.size());
        return PageResponse.<BackgroundShortResponse>builder()
                .items(responseItems)
                .pagination(pagination)
                .build();
    }

    @Transactional
    @Override
    @CacheEvict(cacheNames = "countAllMaterials")
    public String addBackground(final BackgroundRequest request) {
        checkUrlExist(request.getUrl());
        var feat = getFeat(request.getFeatUrl());
        var book = bookService.findByUrl(request.getSource().getUrl());
        return backgroundRepository.save(backgroundMapper.toEntity(request, feat, book))
                .getUrl();
    }

    @Transactional
    @Override
    public String updateBackgrounds(final String url, final BackgroundRequest request) {
        checkUrlExist(request.getUrl());
        if (!url.equals(request.getUrl())) {
            backgroundRepository.deleteById(url);
        }
        var feat = getFeat(request.getFeatUrl());
        var book = bookService.findByUrl(request.getSource().getUrl());
        return backgroundRepository.save(backgroundMapper.toEntity(request, feat, book))
                .getUrl();
    }

    @Transactional
    @Override
    @CacheEvict(cacheNames = "countAllMaterials")
    public String deleteBackgrounds(final String url) {
        var entity = findByUrl(url);
        entity.setHiddenEntity(true);
        return backgroundRepository.save(entity).getUrl();
    }

    @Override
    public boolean exists(final String backgroundUrl) {
        return backgroundRepository.existsById(backgroundUrl);
    }

    @Override
    public BackgroundRequest findFormByUrl(final String url) {
        return backgroundMapper.toRequest(findByUrl(url));
    }

    private Feat getFeat(String url) {
        return featRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Черта не найдена по URL: " + url));
    }

    private void checkUrlExist(String url) {
        if (backgroundRepository.existsById(url)) {
            throw new EntityExistException("Предыстория существует с url: " + url);
        }
    }

    private Background findByUrl(String url) {
        return backgroundRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Предыстория не найден по URL: " + url));
    }

    Specification<Background> containsText(String searchLine) {
        return (root, query, cb) -> {
            String likePattern = "%" + searchLine + "%";
            String invertedSearchLine = SwitchLayoutUtils.switchLayout(searchLine);
            String invertedPattern = "%" + invertedSearchLine + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("name")), likePattern.toLowerCase()),
                    cb.like(cb.lower(root.get("english")), likePattern.toLowerCase()),
                    cb.like(cb.lower(root.get("alternative")), likePattern.toLowerCase()),
                    cb.like(cb.lower(root.get("name")), invertedPattern.toLowerCase()),
                    cb.like(cb.lower(root.get("english")), invertedPattern.toLowerCase()),
                    cb.like(cb.lower(root.get("alternative")), invertedPattern.toLowerCase())
            );
        };
    }
}

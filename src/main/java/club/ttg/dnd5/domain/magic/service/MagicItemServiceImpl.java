package club.ttg.dnd5.domain.magic.service;

import club.ttg.dnd5.domain.book.service.BookService;
import club.ttg.dnd5.domain.common.rest.dto.PageResponse;
import club.ttg.dnd5.domain.common.rest.dto.Pagination;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.repository.MagicItemRepository;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemDetailResponse;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemRequest;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemShortResponse;
import club.ttg.dnd5.domain.magic.rest.mapper.MagicItemMapper;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MagicItemServiceImpl implements MagicItemService {

    private final MagicItemRepository magicItemRepository;
    private final MagicItemMapper magicItemMapper;
    private final MagicItemQueryDslSearchService magicDslSearchService;
    private final BookService bookService;

    @Override
    public boolean existsByUrl(String url) {
        if (!magicItemRepository.existsById(url)) {
            throw new EntityNotFoundException("Предмет не найден по URL: " + url);
        }
        return true;
    }

    @Override
    public MagicItemDetailResponse getItem(String url) {
        return magicItemMapper.toDetail(findByUrl(url));
    }

    @Override
    public MagicItemRequest findFormByUrl(final String url) {
        return magicItemMapper.toRequest(findByUrl(url));
    }

    @Override
    public PageResponse<MagicItemShortResponse> getItems(final String searchLine,
                                                         int page,
                                                         int limit,
                                                         String[] sort,
                                                         final SearchBody searchBody) {
        var responseItems = magicDslSearchService.search(
                        searchLine, page, limit, sort, searchBody)
                .stream()
                .map(magicItemMapper::toShort)
                .toList();
        var pagination = Pagination.of(page,
                limit,
                magicItemRepository.count(),
                magicDslSearchService.count(searchLine, searchBody)
        );
        return PageResponse.<MagicItemShortResponse>builder()
                .items(responseItems)
                .pagination(pagination)
                .build();
    }

    @Transactional
    @Override
    @CacheEvict(cacheNames = "countAllMaterials")
    public String addItem(MagicItemRequest request) {
        exist(request.getUrl());
        var book = bookService.findByUrl(request.getSource().getUrl());
        var entity = magicItemMapper.toEntity(request, book);
        return magicItemRepository.save(entity).getUrl();
    }

    @Transactional
    @Override
    public String updateItem(String url, MagicItemRequest request) {
        findByUrl(url);
        if (!request.getUrl().equals(url)) {
            magicItemRepository.deleteById(url);
        }
        var book = bookService.findByUrl(request.getSource().getUrl());
        var entity = magicItemMapper.toEntity(request, book);
        return magicItemRepository.save(entity).getUrl();
    }

    @Transactional
    @Override
    @CacheEvict(cacheNames = "countAllMaterials")
    public String delete(String url) {
        var item = findByUrl(url);
        item.setHiddenEntity(true);
        return magicItemRepository.save(item).getUrl();
    }

    private void exist(String url) {
        if (magicItemRepository.existsById(url)) {
            throw new EntityExistException();
        }
    }

    private MagicItem findByUrl(String url) {
        return magicItemRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Предмет не найден по URL: " + url));
    }

    public MagicItemDetailResponse preview(final MagicItemRequest request) {
        var book = bookService.findByUrl(request.getSource().getUrl());
        return magicItemMapper.toDetail(magicItemMapper.toEntity(request, book));
    }
}

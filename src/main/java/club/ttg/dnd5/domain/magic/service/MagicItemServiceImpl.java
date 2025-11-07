package club.ttg.dnd5.domain.magic.service;

import club.ttg.dnd5.domain.source.service.SourceService;
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

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class MagicItemServiceImpl implements MagicItemService {

    private final MagicItemRepository magicItemRepository;
    private final MagicItemMapper magicItemMapper;
    private final MagicItemQueryDslSearchService magicItemQueryDslSearchService;
    private final SourceService sourceService;

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
    public Collection<MagicItemShortResponse> getItems(final String searchLine, final SearchBody searchBody) {
        return magicItemQueryDslSearchService.search(searchLine, searchBody)
                .stream()
                .map(magicItemMapper::toShort)
                .toList();
    }

    @Transactional
    @Override
    @CacheEvict(cacheNames = "countAllMaterials")
    public String addItem(MagicItemRequest request) {
        exist(request.getUrl());
        var source = sourceService.findByUrl(request.getSource().getUrl());
        var entity = magicItemMapper.toEntity(request, source);
        return magicItemRepository.save(entity).getUrl();
    }

    @Transactional
    @Override
    public String updateItem(String url, MagicItemRequest request) {
        findByUrl(url);
        if (!request.getUrl().equals(url)) {
            magicItemRepository.deleteById(url);
        }
        var source = sourceService.findByUrl(request.getSource().getUrl());
        var entity = magicItemMapper.toEntity(request, source);
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
        var source = sourceService.findByUrl(request.getSource().getUrl());
        return magicItemMapper.toDetail(magicItemMapper.toEntity(request, source));
    }
}

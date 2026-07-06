package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.item.rest.dto.ItemQueryRequest;
import club.ttg.dnd5.domain.source.service.SourceService;
import club.ttg.dnd5.domain.item.model.*;
import club.ttg.dnd5.domain.item.rest.dto.ItemDetailResponse;
import club.ttg.dnd5.domain.item.rest.dto.ItemRequest;
import club.ttg.dnd5.domain.item.rest.dto.ItemShortResponse;
import club.ttg.dnd5.domain.item.rest.mapper.ItemMapper;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import club.ttg.dnd5.domain.revision.model.RevisionOperation;
import club.ttg.dnd5.domain.revision.service.EntityRevisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {
    public static final String REVISION_ENTITY_TYPE = "item";

    private final ItemRepository itemRepository;
    private final ItemQueryDslService itemQueryDslService;
    private final SourceService sourceService;
    private final ItemMapper itemMapper;
    private final EntityRevisionService revisionService;

    @Override
    public boolean existOrThrow(final String url) {
        if (!itemRepository.existsById(url)) {
            throw new EntityNotFoundException(String.format("Предмет с url %s не существует", url));
        }
        return true;
    }

    @Override
    public ItemRequest findFormByUrl(final String url) {
        var item = findByUrl(url);
        return itemMapper.toRequest(item);
    }

    @Override
    public ItemDetailResponse getItem(final String itemUrl) {
        var item = findByUrl(itemUrl);
        return itemMapper.toDetailResponse(item);
    }


    @Override
    @CacheEvict(cacheNames = "countAllMaterials")
    public String addItem(final ItemRequest request) {
        exist(request.getUrl());
        var item = toItem(request);
        String url = itemRepository.save(item).getUrl();
        revisionService.record(REVISION_ENTITY_TYPE, url, RevisionOperation.CREATE, findFormByUrl(url));
        return url;
    }

    @Override
    @Transactional
    public String updateItem(final String itemUrl, final ItemRequest request) {
        var source = sourceService.findByUrl(request.getSource().getUrl());

        if (itemUrl.equals(request.getUrl())) {
            var existing = findByUrl(itemUrl);
            itemMapper.updateEntity(request, source, existing);
            String url = itemRepository.save(existing).getUrl();
            revisionService.record(REVISION_ENTITY_TYPE, url, RevisionOperation.UPDATE, findFormByUrl(url));
            return url;
        }

        findByUrl(itemUrl);
        exist(request.getUrl());
        itemRepository.deleteById(itemUrl);
        itemRepository.flush();
        String url = itemRepository.save(itemMapper.toEntity(request, source)).getUrl();
        revisionService.record(REVISION_ENTITY_TYPE, url, RevisionOperation.UPDATE, findFormByUrl(url));
        return url;
    }

    @Override
    @CacheEvict(cacheNames = "countAllMaterials")
    public String delete(final String itemUrl) {
        Item item = findByUrl(itemUrl);
        item.setHiddenEntity(true);
        String url = itemRepository.save(item).getUrl();
        revisionService.record(REVISION_ENTITY_TYPE, url, RevisionOperation.DELETE, findFormByUrl(url));
        return url;
    }

    private Item toItem(final ItemRequest request) {
        var source = sourceService.findByUrl(request.getSource().getUrl());
        return itemMapper.toEntity(request, source);
    }

    private void exist(String url) {
        if (itemRepository.existsById(url)) {
            throw new EntityExistException();
        }
    }

    private Item findByUrl(String url) {
        return itemRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Предмет не найден по URL: " + url));
    }

    public ItemDetailResponse preview(final ItemRequest request) {
        return itemMapper.toDetailResponse(toItem(request));
    }

    @Override
    public Collection<ItemShortResponse> search(final ItemQueryRequest request)
    {
        var predicate = ItemPredicateBuilder.build(request);
        return itemQueryDslService.search(predicate, request.getPage(), request.getPageSize())
                .stream()
                .map(itemMapper::toShortResponse)
                .toList();
    }
}

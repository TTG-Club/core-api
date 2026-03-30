package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.source.service.SourceService;
import club.ttg.dnd5.domain.item.model.*;
import club.ttg.dnd5.domain.item.rest.dto.ItemDetailResponse;
import club.ttg.dnd5.domain.item.rest.dto.ItemRequest;
import club.ttg.dnd5.domain.item.rest.dto.ItemShortResponse;
import club.ttg.dnd5.domain.item.rest.mapper.ItemMapper;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemQueryDslService itemQueryDslService;
    private final SourceService sourceService;
    private final ItemMapper itemMapper;

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
        return itemRepository.save(item).getUrl();
    }

    @Override
    public String updateItem(final String itemUrl, final ItemRequest request) {
        findByUrl(itemUrl);
        var source = sourceService.findByUrl(request.getSource().getUrl());
        if (!Objects.equals(itemUrl, request.getUrl())) {
            itemRepository.deleteById(itemUrl);
            itemRepository.flush();
        }
        return itemRepository.save(itemMapper.toEntity(request, source)).getUrl();
    }

    @Override
    @CacheEvict(cacheNames = "countAllMaterials")
    public String delete(final String itemUrl) {
        Item item = findByUrl(itemUrl);
        item.setHiddenEntity(true);
        return itemRepository.save(item).getUrl();
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
    public Collection<ItemShortResponse> search(final club.ttg.dnd5.domain.item.rest.dto.ItemQueryRequest request)
    {
        var predicate = ItemPredicateBuilder.build(request);
        return itemQueryDslService.search(predicate, request.getPage(), request.getPageSize())
                .stream()
                .map(itemMapper::toShortResponse)
                .toList();
    }
}

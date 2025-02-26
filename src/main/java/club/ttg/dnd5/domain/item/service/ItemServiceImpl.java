package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.item.rest.dto.ItemDetailResponse;
import club.ttg.dnd5.domain.item.rest.dto.ItemRequest;
import club.ttg.dnd5.domain.item.rest.dto.ItemShortResponse;
import club.ttg.dnd5.domain.item.rest.mapper.ItemMapper;
import club.ttg.dnd5.exception.ContentNotFoundException;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private ItemMapper itemMapper;

    @Override
    public boolean existsByUrl(final String url) {
        var exists = itemRepository.existsById(url);
        if (!exists) {
            throw new ContentNotFoundException("Item not found by uls: " + url);
        }
        return true;
    }

    @Override
    public ItemDetailResponse getItem(final String itemUrl) {
        return itemMapper.toDetailDto(findByUrl(itemUrl));
    }

    @Override
    public Collection<ItemShortResponse> getItems() {
        return itemRepository.findAll()
                .stream()
                .map(itemMapper::toShortDto)
                .toList();
    }

    @Override
    public ItemDetailResponse addItem(final ItemRequest itemDto) {
        exist(itemDto.getUrl());
        var item = itemMapper.toEntity(itemDto);
        return itemMapper.toDetailDto(itemRepository.save(item));
    }

    @Override
    public ItemDetailResponse updateItem(final String itemUrl, final ItemRequest itemDto) {
        Item item = findByUrl(itemUrl);

        return itemMapper.toDetailDto(itemRepository.save(item));
    }

    @Override
    public ItemShortResponse delete(final String itemUrl) {
        Item item = findByUrl(itemUrl);
        item.setHiddenEntity(true);
        return itemMapper.toShortDto(itemRepository.save(item));
    }

    private void exist(String url) {
        if (itemRepository.existsById(url)) {
            throw new EntityExistException();
        }
    }

    private Item findByUrl(String url) {
        return itemRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with URL: " + url));
    }
}

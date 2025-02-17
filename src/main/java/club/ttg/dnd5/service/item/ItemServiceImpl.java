package club.ttg.dnd5.service.item;

import club.ttg.dnd5.dto.item.ItemDto;
import club.ttg.dnd5.exception.ContentNotFoundException;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.model.item.Item;
import club.ttg.dnd5.repository.item.ItemRepository;
import club.ttg.dnd5.utills.Converter;
import club.ttg.dnd5.utills.item.ItemConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    @Override
    public boolean existsByUrl(final String url) {
        var exists = itemRepository.existsById(url);
        if (!exists) {
            throw new ContentNotFoundException("Item not found by uls: " + url);
        }
        return true;
    }

    @Override
    public ItemDto getItem(final String itemUrl) {
        return toDTO(findByUrl(itemUrl));
    }

    @Override
    public Collection<ItemDto> getItems() {
        return itemRepository.findAll()
                .stream()
                .map(f -> toDTO(f, true))
                .toList();
    }

    @Override
    public ItemDto addItem(final ItemDto itemDto) {
        if (itemRepository.existsById(itemDto.getUrl())) {
            throw new EntityExistException();
        }
        return toDTO(itemRepository.save(toEntity(new Item(), itemDto)));
    }

    @Override
    public ItemDto updateItem(final String itemUrl, final ItemDto itemDto) {
        Item item = findByUrl(itemUrl);
        toEntity(item, itemDto);
        return toDTO(itemRepository.save(item));
    }

    @Override
    public ItemDto delete(final String itemUrl) {
        Item item = findByUrl(itemUrl);
        item.setHiddenEntity(true);
        return toDTO(itemRepository.save(item));
    }

    private ItemDto toDTO(Item item) {
        return toDTO(item, false);
    }

    private ItemDto toDTO(Item item, boolean hideDetails) {
        var dto = new ItemDto();
        if (hideDetails) {
            Converter.MAP_ENTITY_TO_BASE_DTO_WITH_HIDE_DETAILS.apply(dto, item);
        } else {
            Converter.MAP_ENTITY_TO_BASE_DTO.apply(dto, item);
            ItemConverter.MAP_ENTITY_TO_DTO_.apply(dto, item);
        }
        return dto;
    }

    private Item toEntity(Item entity, ItemDto dto) {
        entity.setUrl(dto.getUrl());
        Converter.MAP_BASE_DTO_TO_ENTITY_NAME.apply(dto, entity);
        Converter.MAP_DTO_SOURCE_TO_ENTITY_SOURCE.apply(dto.getSourceDTO(), entity);
        ItemConverter.MAP_DTO_TO_ENTITY.apply(dto, entity);
        return entity;
    }

    private Item findByUrl(String url) {
        return itemRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with URL: " + url));
    }
}

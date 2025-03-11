package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.item.model.*;
import club.ttg.dnd5.domain.item.model.weapon.Weapon;
import club.ttg.dnd5.domain.item.rest.dto.ItemDetailResponse;
import club.ttg.dnd5.domain.item.rest.dto.ItemRequest;
import club.ttg.dnd5.domain.item.rest.dto.ItemShortResponse;
import club.ttg.dnd5.domain.item.rest.mapper.ItemMapper;
import club.ttg.dnd5.exception.ContentNotFoundException;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

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
        var item = findByUrl(itemUrl);
        return switch (item) {
            case Armor armor -> itemMapper.toDetailDto(armor);
            case Weapon weapon -> itemMapper.toDetailDto(weapon);
            case Tool tool -> itemMapper.toDetailDto(tool);
            case Vehicle ship -> itemMapper.toDetailDto(ship);
            case Mount mount -> itemMapper.toDetailDto(mount);
            case Item object -> itemMapper.toDetailDto(object);
        };
    }

    @Override
    public Collection<ItemShortResponse> getItems() {
        return itemRepository.findAll()
                .stream()
                .map(itemMapper::toShortDto)
                .toList();
    }

    @Override
    public String addItem(final ItemRequest itemRequest) {
        exist(itemRequest.getUrl());
        var item = switch(itemRequest.getCategory()) {
            case ITEM -> itemMapper.toItemEntity(itemRequest);
            case ARMOR -> itemMapper.toArmorEntity(itemRequest);
            case WEAPON -> itemMapper.toWeaponEntity(itemRequest);
            case VEHICLE -> itemMapper.toVehicleEntity(itemRequest);
            case MOUNT -> itemMapper.toMountEntity(itemRequest);
            case TOOL -> itemMapper.toToolEntity(itemRequest);
        };

        return itemRepository.save(item).getUrl();
    }

    @Override
    public String updateItem(final String itemUrl, final ItemRequest itemDto) {
        Item item = findByUrl(itemUrl);
        return itemRepository.save(item).getUrl();
    }

    @Override
    public String delete(final String itemUrl) {
        Item item = findByUrl(itemUrl);
        item.setHiddenEntity(true);
        return itemRepository.save(item).getUrl();
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
}

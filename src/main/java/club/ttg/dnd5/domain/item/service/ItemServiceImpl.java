package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.book.service.BookService;
import club.ttg.dnd5.domain.item.model.*;
import club.ttg.dnd5.domain.item.model.weapon.Weapon;
import club.ttg.dnd5.domain.item.rest.dto.ItemDetailResponse;
import club.ttg.dnd5.domain.item.rest.dto.ItemRequest;
import club.ttg.dnd5.domain.item.rest.dto.ItemShortResponse;
import club.ttg.dnd5.domain.item.rest.mapper.ItemMapper;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {
    private static final Sort DEFAULT_SORT = Sort.by("name");
    private final ItemRepository itemRepository;
    private final BookService bookService;
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
        return switch (item) {
            case Armor armor -> itemMapper.toRequest(armor);
            case Weapon weapon -> itemMapper.toRequest(weapon);
            case Tool tool -> itemMapper.toRequest(tool);
            case Vehicle ship -> itemMapper.toRequest(ship);
            case Mount mount -> itemMapper.toRequest(mount);
            case Item object -> itemMapper.toRequest(object);
        };
    }

    @Override
    public ItemDetailResponse getItem(final String itemUrl) {
        var item = findByUrl(itemUrl);
        return switch (item) {
            case Armor armor -> itemMapper.toDetailResponse(armor);
            case Weapon weapon -> itemMapper.toDetailResponse(weapon);
            case Tool tool -> itemMapper.toDetailResponse(tool);
            case Vehicle ship -> itemMapper.toDetailResponse(ship);
            case Mount mount -> itemMapper.toDetailResponse(mount);
            case Item object -> itemMapper.toDetailResponse(object);
        };
    }

    @Override
    public Collection<ItemShortResponse> getItems(String searchLine) {
        return Optional.ofNullable(searchLine)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(line -> {
                    String invertedSearchLine = SwitchLayoutUtils.switchLayout(line);
                    return itemRepository.findBySearchLine(line, invertedSearchLine, DEFAULT_SORT);
                })
                .orElseGet(() -> itemRepository.findAll(DEFAULT_SORT))
                .stream()
                .map(itemMapper::toShortResponse)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(cacheNames = "countAllMaterials")
    public String addItem(final ItemRequest request) {
        exist(request.getUrl());
        var book = bookService.findByUrl(request.getSource().getUrl());
        var item = switch(request.getCategory()) {
            case ITEM -> itemMapper.toItem(request, book);
            case ARMOR -> itemMapper.toArmor(request, book);
            case WEAPON -> itemMapper.toWeapon(request, book);
            case VEHICLE -> itemMapper.toVehicle(request, book);
            case MOUNT -> itemMapper.toMount(request, book);
            case TOOL -> itemMapper.toTool(request, book);
        };

        return itemRepository.save(item).getUrl();
    }

    @Override
    public String updateItem(final String itemUrl, final ItemRequest itemDto) {
        Item item = findByUrl(itemUrl);
        return itemRepository.save(item).getUrl();
    }

    @Override
    @CacheEvict(cacheNames = "countAllMaterials")
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

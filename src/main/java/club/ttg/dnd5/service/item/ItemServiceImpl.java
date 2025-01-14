package club.ttg.dnd5.service.item;

import club.ttg.dnd5.dto.item.ItemDto;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.model.item.Item;
import club.ttg.dnd5.repository.item.ItemRepository;
import club.ttg.dnd5.utills.Converter;
import club.ttg.dnd5.utills.character.ItemConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    @Override
    public ItemDto getItem(final String itemUtl) {
        return toDTO(findByUrl(itemUtl));
    }

    @Override
    public Collection<ItemDto> getItems() {
        return itemRepository.findAll()
                .stream()
                .map(f -> toDTO(f, true))
                .toList();
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

    private Item findByUrl(String url) {
        return itemRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with URL: " + url));
    }
}

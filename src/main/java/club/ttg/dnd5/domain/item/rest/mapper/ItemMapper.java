package club.ttg.dnd5.domain.item.rest.mapper;

import club.ttg.dnd5.domain.item.model.Armor;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.model.Weapon;
import club.ttg.dnd5.domain.item.rest.dto.ItemDetailResponse;
import club.ttg.dnd5.domain.item.rest.dto.ItemRequest;
import club.ttg.dnd5.domain.item.rest.dto.ItemShortResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDetailResponse toDetailDto(final Item item);
    ItemDetailResponse toDetailDto(final Armor item);
    ItemDetailResponse toDetailDto(final Weapon item);

    ItemShortResponse toShortDto(Item item);

    Item toEntity(ItemRequest itemDto);
}

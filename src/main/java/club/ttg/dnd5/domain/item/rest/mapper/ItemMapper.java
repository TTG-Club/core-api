package club.ttg.dnd5.domain.item.rest.mapper;

import club.ttg.dnd5.domain.item.model.Armor;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.model.Weapon;
import club.ttg.dnd5.domain.item.rest.dto.ItemDetailResponse;
import club.ttg.dnd5.domain.item.rest.dto.ItemRequest;
import club.ttg.dnd5.domain.item.rest.dto.ItemShortResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    ItemDetailResponse toDetailDto(final Item item);
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    ItemDetailResponse toDetailDto(final Armor item);
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    ItemDetailResponse toDetailDto(final Weapon item);
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    ItemShortResponse toShortDto(Item item);

    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "name.english", target = "english")
    Item toEntity(ItemRequest itemDto);
}

package club.ttg.dnd5.domain.item.rest.mapper;

import club.ttg.dnd5.domain.item.model.*;
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
    ItemDetailResponse toDetailDto(final Armor armor);
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    ItemDetailResponse toDetailDto(final Weapon weapon);
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    ItemDetailResponse toDetailDto(final Ship ship);
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    ItemDetailResponse toDetailDto(final Mount mount);

    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    ItemShortResponse toShortDto(Item item);

    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "name.english", target = "english")
    Item toEntity(ItemRequest itemDto);
}

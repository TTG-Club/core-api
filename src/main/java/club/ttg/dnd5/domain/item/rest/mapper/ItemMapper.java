package club.ttg.dnd5.domain.item.rest.mapper;

import club.ttg.dnd5.domain.item.model.*;
import club.ttg.dnd5.domain.item.rest.dto.ItemDetailResponse;
import club.ttg.dnd5.domain.item.rest.dto.ItemRequest;
import club.ttg.dnd5.domain.item.rest.dto.ItemShortResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "type", constant = "ITEM")
    ItemDetailResponse toDetailDto(final Item item);

    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "type", constant = "ARMOR")
    ItemDetailResponse toDetailDto(final Armor armor);

    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "type", constant = "WEAPON")
    ItemDetailResponse toDetailDto(final Weapon weapon);

    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "type", constant = "SHIP")
    ItemDetailResponse toDetailDto(final Ship ship);

    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "type", constant = "MOUNT")
    ItemDetailResponse toDetailDto(final Mount mount);

    @BaseMapping.BaseShortResponseNameMapping
    ItemShortResponse toShortDto(Item item);

    @BaseMapping.BaseEntityNameMapping
    Item toEntity(ItemRequest request);
}

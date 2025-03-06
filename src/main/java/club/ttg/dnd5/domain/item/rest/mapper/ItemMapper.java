package club.ttg.dnd5.domain.item.rest.mapper;

import club.ttg.dnd5.domain.item.model.*;
import club.ttg.dnd5.domain.item.rest.dto.ItemDetailResponse;
import club.ttg.dnd5.domain.item.rest.dto.ItemRequest;
import club.ttg.dnd5.domain.item.rest.dto.ItemShortResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {BaseMapping.class})
public interface ItemMapper {
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "category", constant = "ITEM")
    @BaseItem
    ItemDetailResponse toDetailDto(final Item item);

    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "category", constant = "ARMOR")
    @BaseItem
    ItemDetailResponse toDetailDto(final Armor armor);

    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "category", constant = "WEAPON")
    @BaseItem
    ItemDetailResponse toDetailDto(final Weapon weapon);

    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "category", constant = "SHIP")
    @BaseItem
    ItemDetailResponse toDetailDto(final Vehicle ship);

    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(target = "category", constant = "MOUNT")
    @BaseItem
    ItemDetailResponse toDetailDto(final Mount mount);

    @BaseMapping.BaseShortResponseNameMapping
    ItemShortResponse toShortDto(Item item);

    @BaseMapping.BaseEntityNameMapping
    Item toItemEntity(ItemRequest request);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "armorCategory.name", target = "category")
    Armor toArmorEntity(ItemRequest request);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "waponCategory.name", target = "category")
    Weapon toWeaponEntity(ItemRequest request);

    @BaseMapping.BaseEntityNameMapping
    MagicItem toMagicEntity(ItemRequest request);

    @BaseMapping.BaseEntityNameMapping
    Vehicle toVehicleEntity(ItemRequest request);

    @BaseMapping.BaseEntityNameMapping
    Mount toMountEntity(ItemRequest request);

    @BaseMapping.BaseEntityNameMapping
    Tool toToolEntity(ItemRequest request);

    @Named("typeToSting")
    default String typeToSting(Set<ItemType> types) {
        return types.stream().map(ItemType::getName).collect(Collectors.joining(", "));
    }

    @Mapping(source = "types", target = "types", qualifiedByName = "typeToSting")
    @interface BaseItem {}
}

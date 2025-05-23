package club.ttg.dnd5.domain.item.rest.mapper;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.item.model.*;
import club.ttg.dnd5.domain.item.model.weapon.Damage;
import club.ttg.dnd5.domain.item.model.weapon.Weapon;
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
    @BaseMapping.BaseSourceMapping
    @Mapping(target = "category", constant = "ITEM")
    @BaseItem
    ItemDetailResponse toDetailResponse(final Item item);

    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(target = "category", constant = "ARMOR")
    @BaseItem
    ItemDetailResponse toDetailResponse(final Armor armor);

    @BaseItem
    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(target = "category", constant = "WEAPON")
    @Mapping(source = "weaponCategory", target = "weaponCategory")
    @Mapping(source = "damage", target = "damage", qualifiedByName = "damageToString")
    @Mapping(source = "mastery.name", target = "mastery")
    ItemDetailResponse toDetailResponse(final Weapon weapon);

    @BaseItem
    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(target = "category", constant = "SHIP")
    ItemDetailResponse toDetailResponse(final Vehicle ship);

    @BaseItem
    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(target = "category", constant = "MOUNT")
    ItemDetailResponse toDetailResponse(final Mount mount);

    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    ItemShortResponse toShortResponse(Item item);

    @BaseMapping.BaseRequestNameMapping
    @BaseMapping.BaseSourceRequestMapping
    ItemRequest toRequest(Item item);
    @BaseMapping.BaseRequestNameMapping
    @BaseMapping.BaseSourceRequestMapping
    ItemRequest toRequest(Armor armor);
    @BaseMapping.BaseRequestNameMapping
    @BaseMapping.BaseSourceRequestMapping
    ItemRequest toRequest(Weapon weapon);
    @BaseMapping.BaseRequestNameMapping
    @BaseMapping.BaseSourceRequestMapping
    ItemRequest toRequest(Mount mount);
    @BaseMapping.BaseRequestNameMapping
    @BaseMapping.BaseSourceRequestMapping
    ItemRequest toRequest(Tool tool);
    @BaseMapping.BaseRequestNameMapping
    @BaseMapping.BaseSourceRequestMapping
    ItemRequest toRequest(Vehicle vehicle);

    @BaseMapping.BaseEntityNameMapping
    @BaseMapping.BaseSourceMapping
    Item toItem(ItemRequest request, Book source);

    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "armorCategory.name", target = "armorCategory")
    Armor toArmor(ItemRequest request, Book source);

    @BaseMapping.BaseEntityNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(source = "weaponCategory.name", target = "weaponCategory")
    Weapon toWeapon(ItemRequest request, Book source);

    @BaseMapping.BaseEntityNameMapping
    @BaseMapping.BaseSourceMapping
    Vehicle toVehicle(ItemRequest request, Book source);

    @BaseMapping.BaseEntityNameMapping
    @BaseMapping.BaseSourceMapping
    Mount toMount(ItemRequest request, Book source);

    @BaseMapping.BaseEntityNameMapping
    @BaseMapping.BaseSourceMapping
    Tool toTool(ItemRequest request, Book source);

    @Named("damageToString")
    default String damageToString(Damage damage) {
        return damage.toString();
    }

    @Named("typeToSting")
    default String typeToSting(Set<ItemType> types) {
        return types.stream().map(ItemType::getName).collect(Collectors.joining(", "));
    }

    @Mapping(source = "types", target = "types", qualifiedByName = "typeToSting")
    @interface BaseItem {}
}

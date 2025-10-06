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
    @Mapping(source = ".", target = "cost", qualifiedByName = "getCost")
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
    @Mapping(source = ".", target = "cost", qualifiedByName = "getCost")
    ItemShortResponse toShort(Item item);

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
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(target = "source", source = "source")
    Item toItem(ItemRequest request, Book source);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(target = "source", source = "source")
    Armor toArmor(ItemRequest request, Book source);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(target = "source", source = "source")
    Weapon toWeapon(ItemRequest request, Book source);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(target = "source", source = "source")
    Vehicle toVehicle(ItemRequest request, Book source);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(target = "source", source = "source")
    Mount toMount(ItemRequest request, Book source);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(target = "source", source = "source")
    Tool toTool(ItemRequest request, Book source);

    @Named("damageToString")
    default String damageToString(Damage damage) {
        return damage.toString();
    }

    @Named("typeToSting")
    default String typeToSting(Set<ItemType> types) {
        return types.stream().map(ItemType::getName).collect(Collectors.joining(", "));
    }

    @Named("getCost")
    default String getCost(Item item) {
        if (item.getCost() == null) {
            return "варьируется";
        }
        return item.getCost() + " " + item.getCoin().getShortName();
    }

    @Mapping(source = "types", target = "types", qualifiedByName = "typeToSting")
    @interface BaseItem {}
}

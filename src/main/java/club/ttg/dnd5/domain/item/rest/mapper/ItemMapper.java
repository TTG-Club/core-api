package club.ttg.dnd5.domain.item.rest.mapper;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.item.model.*;
import club.ttg.dnd5.domain.item.rest.dto.ItemDetailResponse;
import club.ttg.dnd5.domain.item.rest.dto.ItemRequest;
import club.ttg.dnd5.domain.item.rest.dto.ItemShortResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Comparator;
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
    @Mapping(source = ".", target = "cost", qualifiedByName = "getCost")
    ItemShortResponse toShortResponse(Item item);

    @BaseMapping.BaseRequestNameMapping
    @BaseMapping.BaseSourceRequestMapping
    ItemRequest toRequest(Item item);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(target = "source", source = "source")
    Item toEntity(ItemRequest request, Book source);

    @Named("typeToSting")
    default String typeToSting(Set<ItemType> types) {
        return types.stream()
                .sorted(Comparator.comparing(ItemType::ordinal))
                .map(ItemType::getName)
                .collect(Collectors.joining(", "));
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

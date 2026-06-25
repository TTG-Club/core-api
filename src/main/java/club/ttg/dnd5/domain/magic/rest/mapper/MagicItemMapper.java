package club.ttg.dnd5.domain.magic.rest.mapper;

import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemDetailResponse;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemRequest;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemShortResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.util.StringUtils;

import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", uses = {BaseMapping.class})
public interface MagicItemMapper
{
    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(source = "attunement.requires", target = "attunement")
    @Mapping(source = "rarity.name", target = "rarity")
    MagicItemShortResponse toShort(MagicItem magicItem);

    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(source = ".", target = "subtitle", qualifiedByName = "toSubtitle")
    MagicItemDetailResponse toDetail(MagicItem magicItem);

    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(source = "source.url", target = "source.url")
    @Mapping(source = "sourcePage", target = "source.page")
    @Mapping(target = "category.clarification", source = "clarification")
    @Mapping(target = "category.type", source = "category")
    @Mapping(target = "rarity.type", source = "rarity")
    @Mapping(target = "rarity.varies", source = "varies")
    @Mapping(target = "items", source = "items", qualifiedByName = "itemsToUrls")
    MagicItemRequest toRequest(MagicItem magicItem);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.original", target = "original")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(source = "request.category.clarification", target = "clarification")
    @Mapping(source = "request.category.type", target = "category")
    @Mapping(source = "request.rarity.type", target = "rarity")
    @Mapping(source = "request.rarity.varies", target = "varies")
    @Mapping(source = "request.srdVersion", target = "srdVersion")
    @Mapping(target = "source", source = "source")
    @Mapping(target = "items", source = "linkedItems")
    MagicItem toEntity(MagicItemRequest request, Source source, Set<Item> linkedItems);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(target = "url", ignore = true)
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.original", target = "original")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(source = "request.category.clarification", target = "clarification")
    @Mapping(source = "request.category.type", target = "category")
    @Mapping(source = "request.rarity.type", target = "rarity")
    @Mapping(source = "request.rarity.varies", target = "varies")
    @Mapping(source = "request.srdVersion", target = "srdVersion")
    @Mapping(target = "source", source = "source")
    @Mapping(target = "items", source = "linkedItems")
    void updateEntity(MagicItemRequest request, Source source, Set<Item> linkedItems, @MappingTarget MagicItem magicItem);

    @Named("itemsToUrls")
    default List<String> itemsToUrls(Set<Item> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
                .map(Item::getUrl)
                .sorted()
                .toList();
    }

    @Named("toSubtitle")
    default String toSubtitle(MagicItem magicItem) {
        var builder = new StringBuilder();
        builder.append(StringUtils.capitalize(magicItem.getCategory().getName()));
        if (StringUtils.hasText(magicItem.getClarification())) {
            builder.append(" (");
            builder.append(magicItem.getClarification());
            builder.append(")");
        }
        builder.append(", ");
        if (StringUtils.hasText(magicItem.getVaries())) {
            builder.append(magicItem.getVaries());
        } else {
            builder.append(magicItem.getRarity().getName(magicItem.getCategory()));
        }
        if (magicItem.getAttunement() != null && magicItem.getAttunement().isRequires()) {
            if (StringUtils.hasText(magicItem.getAttunement().getDescription())) {
                builder.append(" (требуется настройка ");
                builder.append(magicItem.getAttunement().getDescription());
                builder.append(")");
            } else {
                builder.append(" (требуется настройка)");
            }
        }
        return builder.toString();
    }
}

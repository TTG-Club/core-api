package club.ttg.dnd5.domain.magic.rest.mapper;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemDetailResponse;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemRequest;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemShortResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.StringUtils;

@Mapper(componentModel = "spring", uses = {BaseMapping.class})
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
    @Mapping(target = "request.category.clarification", source = "clarification")
    @Mapping(target = "request.category.type", source = "category")
    @Mapping(target = "request.rarity.type", source = "rarity")
    @Mapping(target = "request.rarity.varies", source = "varies")
    MagicItemRequest toRequest(MagicItem magicItem);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(source = "request.category.clarification", target = "clarification")
    @Mapping(source = "request.category.type", target = "category")
    @Mapping(source = "request.rarity.type", target = "rarity")
    @Mapping(source = "request.rarity.varies", target = "varies")
    @Mapping(target = "source", source = "source")
    MagicItem toEntity(MagicItemRequest request, Book source);

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

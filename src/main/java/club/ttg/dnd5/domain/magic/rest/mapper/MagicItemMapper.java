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

@Mapper(componentModel = "spring", uses = {BaseMapping.class})
public interface MagicItemMapper
{
    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(source = "attunement.requires", target = "attunement")
    MagicItemShortResponse toShort(MagicItem magicItem);

    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(source = ".", target = "subtitle", qualifiedByName = "toSubtitle")
    MagicItemDetailResponse toDetail(MagicItem magicItem);

    @BaseMapping.BaseShortResponseNameMapping
    MagicItemRequest toRequest(MagicItem magicItem);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(target = "source", source = "source")
    MagicItem toEntity(MagicItemRequest request, Book source);

    @Named("toSubtitle")
    default String toSubtitle(MagicItem magicItem) {
        var builder = new StringBuilder();
        builder.append(magicItem.getCategory().getName());
        if (magicItem.getClarification() != null) {
            builder.append(" (");
            builder.append(magicItem.getClarification());
            builder.append(" )");
        }
        builder.append(", ");
        builder.append(magicItem.getRarity().getName(magicItem.getCategory()));
        if (magicItem.getAttunement() != null) {
            builder.append(" (требуется настройка");
        }
        return builder.toString();
    }
}

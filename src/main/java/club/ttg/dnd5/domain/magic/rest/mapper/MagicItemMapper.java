package club.ttg.dnd5.domain.magic.rest.mapper;

import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemRequest;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemShortResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {BaseMapping.class})
public interface MagicItemMapper
{
    @BaseMapping.BaseShortResponseNameMapping
    MagicItemShortResponse toShortResponse(MagicItem magicItem);
    @BaseMapping.BaseShortResponseNameMapping
    MagicItemShortResponse toDetailResponse(MagicItem magicItem);

    @BaseMapping.BaseEntityNameMapping
    MagicItem toShortResponse(MagicItemRequest request);
}

package club.ttg.dnd5.domain.magic.rest.mapper;

import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemDetailResponse;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemRequest;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemShortResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {BaseMapping.class})
public interface MagicItemMapper
{
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(source = "attunement.requires", target = "attunement")
    MagicItemShortResponse toShortResponse(MagicItem magicItem);

    @BaseMapping.BaseShortResponseNameMapping
    MagicItemDetailResponse toDetailResponse(MagicItem magicItem);

    @BaseMapping.BaseEntityNameMapping
    MagicItem toEntity(MagicItemRequest request);

}

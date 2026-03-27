package club.ttg.dnd5.domain.magic.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Rarity;
import club.ttg.dnd5.domain.filter.rest.FilterParam;
import club.ttg.dnd5.domain.magic.model.MagicItemCategory;
import club.ttg.dnd5.dto.base.filters.AbstractQueryRequest;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import club.ttg.dnd5.dto.base.filters.QuerySingleton;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MagicItemQueryRequest extends AbstractQueryRequest
{
    @FilterParam(enumClass = MagicItemCategory.class)
    private QueryFilter<MagicItemCategory> category;

    @FilterParam(enumClass = Rarity.class)
    private QueryFilter<Rarity> rarity;

    @FilterParam
    private QuerySingleton attunement;

    @FilterParam
    private QuerySingleton charges;

    @FilterParam
    private QuerySingleton curse;
}

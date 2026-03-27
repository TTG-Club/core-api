package club.ttg.dnd5.domain.magic.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Rarity;
import club.ttg.dnd5.domain.magic.model.MagicItemCategory;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import club.ttg.dnd5.dto.base.filters.QuerySingleton;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class MagicItemQueryRequest
{
    private String search;
    private QueryFilter<MagicItemCategory> category;
    private QueryFilter<Rarity> rarity;
    private QuerySingleton attunement;
    private QuerySingleton charges;
    private QuerySingleton curse;
    private Set<String> source = Set.of();
    private int page = 0;
    private int pageSize = 10000;
}

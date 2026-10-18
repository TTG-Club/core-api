package club.ttg.dnd5.domain.magic.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Rarity;
import club.ttg.dnd5.domain.filter.rest.FilterParam;
import club.ttg.dnd5.domain.magic.model.MagicItemCategory;
import club.ttg.dnd5.domain.magic.model.MagicItemTheme;
import club.ttg.dnd5.dto.base.filters.AbstractQueryRequest;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
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

    @FilterParam(enumClass = MagicItemTheme.class)
    private QueryFilter<MagicItemTheme> theme;

    @FilterParam(enumClass = Rarity.class)
    private QueryFilter<Rarity> rarity;

    @FilterParam
    private QueryFilter<String> attunement;

    @FilterParam
    private QueryFilter<String> charges;

    @FilterParam
    private QueryFilter<String> curse;

    /**
     * True — только предметы, которые показываются в списке раздела ({@code showInList}).
     * Передаёт страница списка; выбор предмета в листе персонажа и других полях ищет по всем.
     */
    private boolean listedOnly;
}

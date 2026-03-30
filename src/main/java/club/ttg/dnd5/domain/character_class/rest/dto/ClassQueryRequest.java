package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.filter.rest.FilterParam;
import club.ttg.dnd5.dto.base.filters.AbstractQueryRequest;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClassQueryRequest extends AbstractQueryRequest
{
    @FilterParam(
            enumClass = Dice.class,
            description = "Кости хитов класса"
    )
    private QueryFilter<Dice> hitDice;
}

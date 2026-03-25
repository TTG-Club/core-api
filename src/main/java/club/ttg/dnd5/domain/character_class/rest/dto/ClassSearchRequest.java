package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.dto.base.filters.AbstractSearchRequest;
import club.ttg.dnd5.dto.base.filters.ThreeStateFilter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO запроса фильтрации классов.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClassSearchRequest extends AbstractSearchRequest
{
    /** Кость хитов (3-state enum). */
    private ThreeStateFilter<Dice> hitDice;
}

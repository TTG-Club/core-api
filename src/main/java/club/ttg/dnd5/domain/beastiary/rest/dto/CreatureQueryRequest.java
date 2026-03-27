package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.beastiary.model.sense.CreatureSenses;
import club.ttg.dnd5.domain.common.dictionary.Alignment;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.Habitat;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.domain.filter.rest.FilterParam;
import club.ttg.dnd5.dto.base.filters.AbstractQueryRequest;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import club.ttg.dnd5.dto.base.filters.QuerySingleton;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO запроса фильтрации существ через URL-параметры.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreatureQueryRequest extends AbstractQueryRequest
{
    @FilterParam
    private QueryFilter<Long> cr;

    @FilterParam(enumClass = CreatureType.class)
    private QueryFilter<CreatureType> type;

    @FilterParam(enumClass = Size.class)
    private QueryFilter<Size> size;

    @FilterParam(enumClass = Alignment.class)
    private QueryFilter<Alignment> alignment;

    @FilterParam(enumClass = Habitat.class)
    private QueryFilter<Habitat> habitat;

    @FilterParam(enumClass = CreatureSenses.class)
    private QueryFilter<CreatureSenses> senses;

    @FilterParam
    private QueryFilter<String> traits;

    @FilterParam
    private QueryFilter<String> tag;

    @FilterParam
    private QuerySingleton lair;

    @FilterParam
    private QuerySingleton legendaryAction;
}

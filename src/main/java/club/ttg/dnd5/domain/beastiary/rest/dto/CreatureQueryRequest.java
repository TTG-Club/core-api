package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.beastiary.model.sense.CreatureSenses;
import club.ttg.dnd5.domain.common.dictionary.Alignment;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Habitat;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.domain.filter.rest.FilterParam;
import club.ttg.dnd5.domain.filter.rest.QueryParam;
import club.ttg.dnd5.dto.base.filters.AbstractQueryRequest;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
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
    @QueryParam
    private CreatureGrouping grouping = CreatureGrouping.CHALLENGE_RATING;

    @QueryParam
    private CreatureSorting sorting = CreatureSorting.CHALLENGE_RATING;

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

    /**
     * Уязвимость к урону (колонка {@code vulnerabilities}).
     */
    @FilterParam(enumClass = DamageType.class)
    private QueryFilter<DamageType> vulnerability;

    /**
     * Сопротивление к урону (колонка {@code resistance}).
     */
    @FilterParam(enumClass = DamageType.class)
    private QueryFilter<DamageType> resistance;

    /**
     * Иммунитет к урону (колонка {@code immunity_to_damage}).
     */
    @FilterParam(enumClass = DamageType.class)
    private QueryFilter<DamageType> damageImmunity;

    /**
     * Иммунитет к состояниям (колонка {@code immunity_to_condition}).
     */
    @FilterParam(enumClass = Condition.class)
    private QueryFilter<Condition> conditionImmunity;

    @FilterParam
    private QueryFilter<String> traits;

    @FilterParam
    private QueryFilter<String> tag;

    @FilterParam
    private QueryFilter<String> lair;

    @FilterParam
    private QueryFilter<String> legendaryAction;
}

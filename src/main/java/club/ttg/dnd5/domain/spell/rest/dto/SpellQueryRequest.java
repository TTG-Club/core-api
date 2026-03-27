package club.ttg.dnd5.domain.spell.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.HealingType;
import club.ttg.dnd5.domain.filter.rest.FilterParam;
import club.ttg.dnd5.domain.spell.model.enums.MagicSchool;
import club.ttg.dnd5.dto.base.filters.AbstractQueryRequest;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import club.ttg.dnd5.dto.base.filters.QuerySingleton;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SpellQueryRequest extends AbstractQueryRequest
{
    @FilterParam(enumClass = MagicSchool.class)
    private QueryFilter<MagicSchool> school;

    @FilterParam
    private QueryFilter<Long> level;

    @FilterParam
    private QueryFilter<String> className;

    @FilterParam
    private QueryFilter<String> subclassName;

    @FilterParam(enumClass = DamageType.class)
    private QueryFilter<DamageType> damageType;

    @FilterParam(enumClass = HealingType.class)
    private QueryFilter<HealingType> healingType;

    @FilterParam(enumClass = Condition.class)
    private QueryFilter<Condition> condition;

    @FilterParam(enumClass = Ability.class)
    private QueryFilter<Ability> savingThrow;

    @FilterParam
    private QuerySingleton ritual;

    @FilterParam
    private QuerySingleton concentration;

    @FilterParam
    private QuerySingleton upcastable;

    @FilterParam
    private QueryFilter<String> castingTime;

    @FilterParam
    private QueryFilter<String> duration;
}

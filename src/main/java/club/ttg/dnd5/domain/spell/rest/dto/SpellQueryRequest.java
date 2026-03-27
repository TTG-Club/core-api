package club.ttg.dnd5.domain.spell.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.HealingType;
import club.ttg.dnd5.domain.spell.model.enums.MagicSchool;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import club.ttg.dnd5.dto.base.filters.QuerySingleton;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class SpellQueryRequest
{
    private String search;
    private QueryFilter<MagicSchool> school;
    private QueryFilter<Long> level;
    private QueryFilter<String> className;
    private QueryFilter<String> subclassName;
    private QueryFilter<DamageType> damageType;
    private QueryFilter<HealingType> healingType;
    private QueryFilter<Condition> condition;
    private QueryFilter<Ability> savingThrow;
    private QuerySingleton ritual;
    private QuerySingleton concentration;
    private QuerySingleton upcastable;
    private QueryFilter<String> castingTime;
    private QueryFilter<String> duration;
    private Set<String> source = Set.of();
    private int page = 0;
    private int pageSize = 10000;
}

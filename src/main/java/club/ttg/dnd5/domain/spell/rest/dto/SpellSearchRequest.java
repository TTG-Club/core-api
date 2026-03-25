package club.ttg.dnd5.domain.spell.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.HealingType;
import club.ttg.dnd5.domain.spell.model.enums.MagicSchool;
import club.ttg.dnd5.dto.base.filters.AbstractSearchRequest;
import club.ttg.dnd5.dto.base.filters.ThreeStateFilter;
import club.ttg.dnd5.dto.base.filters.ThreeStateSingleton;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO запроса фильтрации заклинаний.
 * Десериализуется из Base64url JSON параметра {@code f}.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SpellSearchRequest extends AbstractSearchRequest
{
    /**
     * Школа магии (3-state: include/exclude).
     */
    private ThreeStateFilter<MagicSchool> school;

    /**
     * Уровень заклинания (3-state).
     */
    private ThreeStateFilter<Long> level;

    /**
     * Классы (3-state по url класса).
     */
    private ThreeStateFilter<String> className;

    /**
     * Подклассы (3-state по url подкласса).
     */
    private ThreeStateFilter<String> subclassName;

    /**
     * Тип урона (3-state, JSONB-массив).
     */
    private ThreeStateFilter<DamageType> damageType;

    /**
     * Тип лечения (3-state, JSONB-массив).
     */
    private ThreeStateFilter<HealingType> healingType;

    /**
     * Состояния (3-state, JSONB-массив).
     */
    private ThreeStateFilter<Condition> condition;

    /**
     * Спасброски (3-state, JSONB-массив).
     */
    private ThreeStateFilter<Ability> savingThrow;

    /**
     * Ритуал (3-state singleton).
     */
    private ThreeStateSingleton ritual;

    /**
     * Концентрация (3-state singleton).
     */
    private ThreeStateSingleton concentration;

    /**
     * Улучшается с уровнем ячейки (3-state singleton).
     */
    private ThreeStateSingleton upcastable;
}

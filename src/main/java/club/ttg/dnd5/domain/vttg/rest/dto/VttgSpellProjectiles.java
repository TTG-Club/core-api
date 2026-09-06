package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Снарядный режим заклинания в формате компендиума VTTG ({@code SpellProjectiles}).
 *
 * <p>Волшебная стрела, Мистический заряд, Палящий луч: каждый снаряд — отдельный
 * бросок урона (и попадания, если заклинание атакующее), снаряды распределяются
 * по целям на касте. Без этого блока потребитель считает заклинание одноцелевым,
 * поэтому {@code count} обязателен: блок без числа снарядов не выгружается.</p>
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgSpellProjectiles {
    /** Базовое число снарядов (до первого порога уровня персонажа). */
    private Integer count;
    /** Доп. снарядов за круг ячейки выше базового (уровневые заклинания). */
    private Integer perSlotLevel;
    /** Пороги уровня персонажа → полное число снарядов (заговоры). */
    private List<VttgProjectileCountTier> countByCharacterLevel;
    /**
     * Распределение снарядов по целям: {@code single} — все в одну цель,
     * {@code distinct} — каждый в свою. Не задано — свободно.
     */
    private String targetDistribution;
}

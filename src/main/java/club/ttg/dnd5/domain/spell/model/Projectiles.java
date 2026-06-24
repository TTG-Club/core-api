package club.ttg.dnd5.domain.spell.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Снарядный режим заклинания (Волшебная стрела, Мистический заряд, Палящий луч):
 * каждый снаряд — отдельный бросок урона/атаки, снаряды распределяются по целям.
 * Зеркало {@code SpellProjectiles} из VTTG (passthrough, без словарей).
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Projectiles {
    /** Базовое число снарядов. */
    private Integer count;
    /** Доп. снарядов за круг ячейки выше базового (уровневые заклинания). */
    private Integer perSlotLevel;
    /** Пороги уровня персонажа → полное число снарядов (заговоры). */
    private List<ProjectileCountTier> countByCharacterLevel;
    /** Распределение снарядов по целям: {@code single} | {@code distinct}. */
    private String targetDistribution;

    /** Порог уровня персонажа → полное число снарядов. */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProjectileCountTier {
        /** Минимальный уровень персонажа, с которого действует это число. */
        private Integer level;
        /** Полное число снарядов начиная с этого уровня (заменяет базовое). */
        private Integer count;
    }
}

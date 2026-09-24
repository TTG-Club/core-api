package club.ttg.dnd5.domain.bastion.model;

import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * Общие числа правил бастиона, не привязанные к конкретному сооружению. Отдаются сайту и
 * будущей мини-игре через {@code GET /api/v2/bastions/rules}.
 */
@UtilityClass
public class BastionRules {
    /** Уровень персонажа → сколько всего специализированных сооружений в бастионе. */
    public static final List<Progression> SPECIAL_FACILITY_PROGRESSION = List.of(
            new Progression(5, 2),
            new Progression(9, 4),
            new Progression(13, 5),
            new Progression(17, 6));

    /** Базовые сооружения, с которыми бастион появляется: одно тесное и одно вместительное. */
    public static final List<FacilitySpace> STARTING_BASIC_FACILITIES = List.of(FacilitySpace.CRAMPED, FacilitySpace.ROOMY);

    /** Высота защитной стены, футы. */
    public static final int DEFENSIVE_WALL_HEIGHT = 20;
    /** Стоимость 5-футового квадрата защитной стены, зм. */
    public static final int DEFENSIVE_WALL_COST_PER_SQUARE = 250;
    /** Время постройки 5-футового квадрата защитной стены, дни. */
    public static final int DEFENSIVE_WALL_DAYS_PER_SQUARE = 10;
    /** На сколько меньше кубиков потерь защитников при атаке на полностью окружённый стенами бастион. */
    public static final int DEFENSIVE_WALL_DEFENDER_DICE_REDUCTION = 2;

    /**
     * Сколько специализированных сооружений положено персонажу этого уровня. До 5 уровня
     * бастиона нет — ноль.
     */
    public static int specialFacilityLimit(int characterLevel) {
        int limit = 0;
        for (Progression progression : SPECIAL_FACILITY_PROGRESSION) {
            if (characterLevel >= progression.level()) {
                limit = progression.count();
            }
        }
        return limit;
    }

    public record Progression(int level, int count) {
    }
}

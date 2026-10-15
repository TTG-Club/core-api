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

    /** Сколько защитников бесплатно нанимает один приказ «Завербовать» в бараке. */
    public static final int BARRACK_DEFENDERS_PER_RECRUIT = 4;
    /** Сколько защитников помещается во вместительном бараке. */
    public static final int BARRACK_ROOMY_CAPACITY = 12;
    /** Сколько защитников помещается в просторном (расширенном) бараке. */
    public static final int BARRACK_VAST_CAPACITY = 24;

    /** Сколько защитников помещается в бараке этого пространства. */
    public static int barrackCapacity(FacilitySpace space) {
        return space == FacilitySpace.VAST ? BARRACK_VAST_CAPACITY : BARRACK_ROOMY_CAPACITY;
    }

    /** Сколько зм стоимости предмета изготавливается за один 8-часовой рабочий день. */
    public static final int CRAFT_GP_PER_DAY = 10;

    /**
     * Сколько дней мастерская изготавливает предмет: покупная стоимость в зм делится на
     * {@value #CRAFT_GP_PER_DAY} за каждый 8-часовой день, наёмники работают одновременно и
     * делят работу. Дробная часть округляется до полного дня, меньше дня не бывает.
     *
     * @param priceGp   покупная стоимость предмета, зм
     * @param hirelings наёмники сооружения; пусто — один
     */
    public static int craftDays(double priceGp, Integer hirelings) {
        int workers = hirelings == null || hirelings < 1 ? 1 : hirelings;
        return Math.max(1, (int) Math.ceil(priceGp / ((double) CRAFT_GP_PER_DAY * workers)));
    }

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

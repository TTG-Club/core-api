package club.ttg.dnd5.domain.bastion.model;

import club.ttg.dnd5.domain.common.dictionary.Size;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Optional;

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

    /**
     * Вместимость вольеров зверинца в четвертях большого существа: 4 больших, а каждые
     * 4 маленьких или средних занимают место одного большого.
     */
    public static final int MENAGERIE_CAPACITY = 16;
    /** Сколько дней наёмники зверинца вербуют существо. */
    public static final int MENAGERIE_RECRUIT_DAYS = 7;
    /**
     * Цена существа не из таблицы зверинца по показателю опасности, зм. Существ опаснее
     * ПО 3 зверинец не принимает.
     */
    public static final List<ChallengeCost> MENAGERIE_CHALLENGE_COSTS = List.of(
            new ChallengeCost("0", 50),
            new ChallengeCost("1/8", 50),
            new ChallengeCost("1/4", 250),
            new ChallengeCost("1/2", 500),
            new ChallengeCost("1", 1000),
            new ChallengeCost("2", 2000),
            new ChallengeCost("3", 3500));

    /**
     * Цена существа не из таблицы зверинца.
     *
     * @param challenge показатель опасности: «1/4», «2»
     * @return цена, зм; пусто — зверинец такое существо не принимает
     */
    public static Optional<Integer> menagerieChallengeCost(String challenge) {
        String value = challenge == null ? "" : challenge.trim();
        return MENAGERIE_CHALLENGE_COSTS.stream()
                .filter(item -> item.challenge().equals(value))
                .map(ChallengeCost::cost)
                .findFirst();
    }

    /**
     * Сколько места в вольерах занимает существо этого размера, в четвертях большого.
     *
     * @return 4 у большого, 1 у крошечного, маленького и среднего; 0 — в зверинце не держат
     */
    public static int menagerieSpace(Size size) {
        if (size == null) {
            return 0;
        }
        return switch (size) {
            case TINY, SMALL, MEDIUM -> 1;
            case LARGE -> 4;
            default -> 0;
        };
    }

    /**
     * Вместимость конюшни в половинах большого животного: 3 больших, а 2 средних занимают
     * место одного большого.
     */
    public static final int STABLE_CAPACITY = 6;
    /** Сколько дней наёмник конюшни покупает или продаёт животное. */
    public static final int STABLE_TRADE_DAYS = 7;
    /** Сколько дней животное живёт в конюшне, чтобы проверки Ухода за животными шли с преимуществом. */
    public static final int STABLE_BOND_DAYS = 14;
    /** На сколько процентов дороже обычной цены покупатель платит за животное из конюшни. */
    public static final int STABLE_SALE_BONUS_PERCENT = 20;
    /** Наценка при продаже с {@link #STABLE_SALE_BONUS_HIGH_LEVEL} уровня, процентов. */
    public static final int STABLE_SALE_BONUS_HIGH_PERCENT = 50;
    public static final int STABLE_SALE_BONUS_HIGH_LEVEL = 13;

    /**
     * Сколько места в конюшне занимает животное этого размера, в половинах большого.
     *
     * @return 2 у большого, 1 у крошечного, маленького и среднего; 0 — в конюшне не держат
     */
    public static int stableSpace(Size size) {
        if (size == null) {
            return 0;
        }
        return switch (size) {
            case TINY, SMALL, MEDIUM -> 1;
            case LARGE -> 2;
            default -> 0;
        };
    }

    /** Наценка при продаже животного из конюшни персонажем этого уровня, процентов. */
    public static int stableSaleBonusPercent(int characterLevel) {
        return characterLevel >= STABLE_SALE_BONUS_HIGH_LEVEL
                ? STABLE_SALE_BONUS_HIGH_PERCENT
                : STABLE_SALE_BONUS_PERCENT;
    }

    /** Сколько платит покупатель за животное из конюшни, зм: обычная цена с наценкой. */
    public static long stableSalePrice(int cost, int characterLevel) {
        return (long) cost * (100 + stableSaleBonusPercent(characterLevel)) / 100;
    }

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

    /** Цена существа зверинца по показателю опасности. */
    public record ChallengeCost(String challenge, int cost) {
    }
}

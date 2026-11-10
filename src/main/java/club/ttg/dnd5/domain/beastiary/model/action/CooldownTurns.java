package club.ttg.dnd5.domain.beastiary.model.action;

import club.ttg.dnd5.domain.common.dictionary.RechargeType;

/**
 * Ограничение «раз в N ходов» у записи существа и у порции заклинаний: потраченное
 * в ход T снова доступно в ход T + N, без броска.
 *
 * <p>В книгах 2024 его нет — оно для самодельных существ мастерской. Диапазон задаёт
 * система VTTG: целое 2–20 (раз в ход — это просто «без ограничения»). С перезарядкой
 * оно взаимоисключающее: если заданы оба, система берёт перезарядку, поэтому здесь
 * значение рядом с перезарядкой не хранится и не выгружается.</p>
 */
public final class CooldownTurns {
    /** Наименьшее число ходов, которое примет система. */
    public static final int MIN = 2;

    /** Наибольшее число ходов, которое примет система. */
    public static final int MAX = 20;

    private CooldownTurns() {
    }

    /**
     * Проверяет число ходов на диапазон системы.
     *
     * @param cooldownTurns число ходов; {@code null} — ограничения нет.
     * @return {@code true}, если число задано и лежит в 2–20.
     */
    public static boolean isValid(Integer cooldownTurns) {
        return cooldownTurns != null && cooldownTurns >= MIN && cooldownTurns <= MAX;
    }

    /**
     * Число ходов записи, которое можно хранить и выгружать.
     *
     * <p>Вне 2–20 система значение не примет, а рядом с перезарядкой оно не действует —
     * в обоих случаях ограничения нет: пусть лучше его не будет, чем оно будет неверным.</p>
     *
     * @param recharge перезарядка записи.
     * @param cooldownTurns число ходов записи.
     * @return число ходов или {@code null}, если ограничения нет.
     */
    public static Integer resolve(RechargeType recharge, Integer cooldownTurns) {
        return recharge == null && isValid(cooldownTurns) ? cooldownTurns : null;
    }
}

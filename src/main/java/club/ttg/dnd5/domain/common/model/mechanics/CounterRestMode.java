package club.ttg.dnd5.domain.common.model.mechanics;

/**
 * Сколько зарядов возвращает ресурсу один вид отдыха.
 */
public enum CounterRestMode {
    /** Отдых ресурс не восстанавливает. */
    NONE,
    /** Отдых возвращает все заряды. */
    ALL,
    /** Отдых возвращает заданное число зарядов ({@link CounterRestRule#getAmount()}). */
    AMOUNT
}

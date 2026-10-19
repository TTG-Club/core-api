package club.ttg.dnd5.domain.spell.model.enums;

/**
 * Что спасбросок цели делает с уроном: {@code HALF} — успех вдвое уменьшает урон,
 * {@code NONE} — успех снимает урон, {@code SPECIAL} — исход разбирает ведущий,
 * {@code DOUBLE} — успех ничего не меняет, а провал удваивает урон
 * («Смертельный удар» Убийцы). В выгрузку VTTG уходит строчными буквами.
 */
public enum SpellSaveEffect {
    HALF,
    NONE,
    SPECIAL,
    DOUBLE
}

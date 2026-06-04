package club.ttg.dnd5.domain.charlist.model;

/**
 * Уровень доступа к чарлисту.
 */
public enum CharlistVisibility {
    /** Приватный — виден только владельцу */
    PRIVATE,
    /** Доступен по ссылке */
    LINK,
    /** Публичный — виден всем */
    PUBLIC
}

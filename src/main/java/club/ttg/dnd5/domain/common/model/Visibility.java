package club.ttg.dnd5.domain.common.model;

/**
 * Уровень видимости пользовательского (homebrew) контента.
 * <p>
 * Применяется только к контенту с владельцем ({@code ownerId != null}). Официальный контент
 * ({@code ownerId == null}) в этой градации не участвует — он всегда доступен всем.
 * <p>
 * Семантически соответствует булевой модели {@code Article} (active/accessibleByLink/draft),
 * но выражен одним перечислением, т.к. для контента статусы взаимоисключающие.
 */
public enum Visibility {

    /** Виден только владельцу. Аналог {@code Article.draft}. */
    PRIVATE,

    /** Не показывается в списках, но доступен по прямой ссылке (url). Аналог {@code Article.accessibleByLink}. */
    UNLISTED,

    /** Доступен всем и попадает в общие списки. Аналог {@code Article.active}. */
    PUBLIC
}

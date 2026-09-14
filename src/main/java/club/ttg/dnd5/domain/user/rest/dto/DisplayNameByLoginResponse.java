package club.ttg.dnd5.domain.user.rest.dto;

/**
 * Пара «логин → отображаемое имя» (и аватарка, если есть) для резолва публичных рейтингов.
 */
public record DisplayNameByLoginResponse(String login, String displayName, String avatarUrl) {
}

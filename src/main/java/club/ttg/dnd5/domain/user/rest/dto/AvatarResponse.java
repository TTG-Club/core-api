package club.ttg.dnd5.domain.user.rest.dto;

/**
 * Ответ с текущей ссылкой на аватарку пользователя ({@code null}, если аватарки нет).
 */
public record AvatarResponse(String avatarUrl) {
}

package club.ttg.dnd5.domain.user.rest.dto;

/**
 * Ответ с текущим отображаемым именем пользователя и ссылкой на его аватарку
 * ({@code null}, если аватарки нет).
 */
public record DisplayNameResponse(String displayName, String avatarUrl) {
}

package club.ttg.dnd5.domain.user.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Запрос на смену аватарки: ссылка на файл, который core-app уже положил в S3.
 * То, что ссылка ведёт в папку текущего пользователя, проверяет сервис.
 */
public record UpdateAvatarRequest(
        @NotBlank(message = "Ссылка на аватарку не может быть пустой")
        @Size(max = 255, message = "Ссылка на аватарку не длиннее 255 символов")
        String avatarUrl) {
}

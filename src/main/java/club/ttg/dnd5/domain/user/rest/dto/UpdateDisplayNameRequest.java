package club.ttg.dnd5.domain.user.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Запрос на смену отображаемого имени. Формат дублирует клиентскую схему:
 * буквы (в т.ч. кириллица), цифры, пробелы, дефисы и подчёркивания, 2–50 символов.
 * Остальные проверки (уникальность, зарезервированные слова, чужой логин) — в сервисе.
 */
public record UpdateDisplayNameRequest(
        @NotBlank(message = "Имя не может быть пустым")
        @Size(min = 2, max = 24, message = "Имя должно быть от 2 до 24 символов")
        @Pattern(
                regexp = "^[\\p{L}\\p{N}_\\s-]+$",
                message = "Только буквы, цифры, пробелы, дефисы и подчёркивания")
        String displayName) {
}

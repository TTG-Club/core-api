package club.ttg.dnd5.domain.user.rest.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Запрос на резолв отображаемых имён по логинам (публичный, для рейтингов).
 * Размер списка ограничивается в сервисе.
 */
public record DisplayNamesLookupRequest(
        @NotNull(message = "logins обязателен")
        List<String> logins) {
}

package club.ttg.dnd5.domain.user.rest.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Запрос на резолв отображаемых имён по идентификаторам пользователей.
 * Размер списка ограничивается в сервисе.
 */
public record DisplayNamesByIdsLookupRequest(
        @NotNull(message = "userIds обязателен")
        List<UUID> userIds) {
}

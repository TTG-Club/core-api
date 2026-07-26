package club.ttg.dnd5.domain.telegram.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Межсервисный запрос одноразовой ссылки-приглашения в секретный чат разработки.
 *
 * @param name подпись ссылки в списке приглашений чата (обычно сам промокод);
 *             Telegram ограничивает её 32 символами — лишнее обрезается
 */
public record CreateDevChatInviteRequest(
        @NotBlank String name
) {
}

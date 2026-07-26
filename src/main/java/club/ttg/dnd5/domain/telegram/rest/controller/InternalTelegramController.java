package club.ttg.dnd5.domain.telegram.rest.controller;

import club.ttg.dnd5.domain.telegram.rest.dto.CreateDevChatInviteRequest;
import club.ttg.dnd5.domain.telegram.rest.dto.DevChatInviteResponse;
import club.ttg.dnd5.domain.telegram.service.TelegramInviteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Внутренняя ручка Telegram для других сервисов (subscriber-service): выпуск
 * одноразовых ссылок-приглашений в секретный чат разработки.
 * Защита — общий секрет {@code X-Service-Token} в {@code InternalServiceTokenFilter},
 * а не JWT; на уровне Spring Security путь {@code /api/internal/**} открыт.
 */
@Tag(name = "Internal: Telegram")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/telegram")
public class InternalTelegramController {
    private final TelegramInviteService inviteService;

    @Operation(summary = "Выпустить одноразовую ссылку-приглашение в чат разработки (межсервисный вызов)")
    @PostMapping("/dev-chat-invite")
    public DevChatInviteResponse createDevChatInvite(@Valid @RequestBody CreateDevChatInviteRequest request) {
        return new DevChatInviteResponse(inviteService.createDevChatInvite(request.name()));
    }
}

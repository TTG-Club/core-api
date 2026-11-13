package club.ttg.dnd5.domain.common.rest.controller;

import club.ttg.dnd5.domain.common.model.OnlineType;
import club.ttg.dnd5.domain.common.service.OnlineUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/online")
@Tag(name = "Онлайн",
        description = "REST API онлайна: кто сейчас на сайте и пользуются ли его инструментами. "
                + "Считает всё online-service, эти ручки — посредник к нему")
public class OnlineUserController
{
    private static final String VISITOR_COOKIE = "visitorId";

    private final OnlineUserService service;

    @Operation(summary = "Отметить, что посетитель на сайте, и узнать, сколько сейчас онлайн. Открыто всем. "
            + "Без тела посетитель определяется по авторизации или по cookie visitorId (гостю она выставляется)")
    @PostMapping("/heartbeat")
    public ResponseEntity<OnlineUserService.HeartbeatResponse> heartbeat(
            @CookieValue(name = VISITOR_COOKIE, required = false) String visitorId,
            Authentication authentication,
            @Valid @RequestBody(required = false) OnlineHeartbeatRequest request
    )
    {
        if (request != null)
        {
            OnlineUserService.HeartbeatResponse response = service.heartbeat(
                    request.type(),
                    request.key(),
                    request.previousGuestKey(),
                    request.preferences()
            );

            return ResponseEntity.ok(response);
        }

        if (isAuthenticated(authentication))
        {
            String userKey = buildRegisteredKey(authentication);
            OnlineUserService.HeartbeatResponse response = service.heartbeat(
                    OnlineType.REGISTERED,
                    userKey,
                    visitorId,
                    null
            );

            return ResponseEntity.ok(response);
        }

        String ensuredVisitorId = (visitorId == null || visitorId.isBlank())
                ? UUID.randomUUID().toString()
                : visitorId;

        OnlineUserService.HeartbeatResponse response = service.heartbeat(
                OnlineType.GUEST,
                ensuredVisitorId,
                null,
                null
        );

        if (visitorId == null || visitorId.isBlank())
        {
            ResponseCookie cookie = ResponseCookie.from(VISITOR_COOKIE, ensuredVisitorId)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(Duration.ofDays(365))
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Сайт раз в минуту сообщает, сколько секунд посетитель работал с инструментом. Открыто всем:
     * гости тоже пользуются инструментами.
     */
    @Operation(summary = "Отметить время работы посетителя с инструментом сайта (например, ширмой Мастера). "
            + "Открыто всем, слать раз в минуту, пока с инструментом работают. Ответ — 204; "
            + "нарушение правил полей — 400")
    @PostMapping("/usage")
    public ResponseEntity<Void> usage(@Valid @RequestBody OnlineUsageRequest request)
    {
        service.usage(request.feature(), request.key(), request.type(), request.activeSeconds());

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Сколько посетителей было на сайте за последние windowMinutes минут "
            + "(от 1 до 1440): гости, зарегистрированные и всего")
    @GetMapping("/count")
    public Map<String, Object> count(@RequestParam(defaultValue = "30") long windowMinutes)
    {
        long clampedMinutes = Math.clamp(windowMinutes, 1, 24 * 60);
        Duration window = Duration.ofMinutes(clampedMinutes);

        OnlineUserService.OnlineCount count = service.getCount(window);

        return Map.of(
                "windowMinutes", window.toMinutes(),
                "guests", count.guests(),
                "registered", count.registered(),
                "total", count.total()
        );
    }

    @Operation(summary = "Статистика для админки по всем сайтам: онлайн, настройки отображения "
            + "и работа с инструментами за разные периоды. Только для роли ADMIN")
    @Secured("ADMIN")
    @GetMapping("/stats")
    public OnlineUserService.OnlineAdminStatsResponse stats()
    {
        return service.getStats();
    }

    private static boolean isAuthenticated(Authentication authentication)
    {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private static String buildRegisteredKey(Authentication authentication)
    {
        // Минимально: authentication.getName()
        // Лучше: реальный userId из principal (см. ниже)
        return authentication.getName();
    }

    @Schema(description = "Отметка о том, что посетитель на сайте")
    public record OnlineHeartbeatRequest(
            @Schema(description = "Кто на сайте: имя пользователя или UUID гостя")
            @NotBlank
            @Size(max = 128)
            String key,

            @Schema(description = "UUID, под которым посетитель был гостем до входа")
            @Size(max = 128)
            String previousGuestKey,

            @Schema(description = "Гость или зарегистрированный пользователь")
            @NotNull
            OnlineType type,

            /*
             * Настройки отображения посетителя (тема, ширина сайта) для статистики в админке.
             * Не обязательны: online-service сам отбросит то, что не отслеживает.
             */
            @Schema(description = "Настройки отображения посетителя (тема, ширина сайта): имя настройки - значение")
            @Size(max = 8)
            Map<String, String> preferences
    ) {}

    @Schema(description = "Отметка о времени работы посетителя с инструментом сайта")
    public record OnlineUsageRequest(
            @Schema(description = "Инструмент сайта", example = "dm-screen")
            @NotBlank
            @Size(max = 32)
            String feature,

            @Schema(description = "Кто работал: имя пользователя или UUID гостя")
            @NotBlank
            @Size(max = 128)
            String key,

            @Schema(description = "Гость или зарегистрированный пользователь")
            @NotNull
            OnlineType type,

            @Schema(description = "Сколько секунд посетитель работал с инструментом с прошлой отметки", example = "60")
            @NotNull
            @Min(0)
            @Max(300)
            Integer activeSeconds
    ) {}
}

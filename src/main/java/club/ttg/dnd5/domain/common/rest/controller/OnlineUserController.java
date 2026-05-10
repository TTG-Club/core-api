package club.ttg.dnd5.domain.common.rest.controller;

import club.ttg.dnd5.domain.common.model.OnlineType;
import club.ttg.dnd5.domain.common.service.OnlineUserService;
import jakarta.validation.Valid;
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
public class OnlineUserController
{
    private static final String VISITOR_COOKIE = "visitorId";

    private final OnlineUserService service;

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
                    request.previousGuestKey()
            );

            return ResponseEntity.ok(response);
        }

        if (isAuthenticated(authentication))
        {
            String userKey = buildRegisteredKey(authentication);
            OnlineUserService.HeartbeatResponse response = service.heartbeat(
                    OnlineType.REGISTERED,
                    userKey,
                    visitorId
            );

            return ResponseEntity.ok(response);
        }

        String ensuredVisitorId = (visitorId == null || visitorId.isBlank())
                ? UUID.randomUUID().toString()
                : visitorId;

        OnlineUserService.HeartbeatResponse response = service.heartbeat(
                OnlineType.GUEST,
                ensuredVisitorId,
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

    public record OnlineHeartbeatRequest(
            @NotBlank
            @Size(max = 128)
            String key,

            @Size(max = 128)
            String previousGuestKey,

            @NotNull
            OnlineType type
    ) {}
}

package club.ttg.dnd5.domain.common.rest.controller;

import club.ttg.dnd5.domain.common.model.OnlineType;
import club.ttg.dnd5.domain.common.service.OnlineUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
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
    public ResponseEntity<Void> heartbeat(
            @CookieValue(name = VISITOR_COOKIE, required = false) String visitorId,
            Authentication authentication
    )
    {
        Instant now = Instant.now();

        if (isAuthenticated(authentication))
        {
            String userKey = buildRegisteredKey(authentication);
            service.heartbeat(OnlineType.REGISTERED, userKey, now);
            return ResponseEntity.noContent().build();
        }

        String ensuredVisitorId = (visitorId == null || visitorId.isBlank())
                ? UUID.randomUUID().toString()
                : visitorId;

        service.heartbeat(OnlineType.GUEST, ensuredVisitorId, now);

        if (visitorId == null || visitorId.isBlank())
        {
            ResponseCookie cookie = ResponseCookie.from(VISITOR_COOKIE, ensuredVisitorId)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(Duration.ofDays(365))
                    .build();

            return ResponseEntity.noContent()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .build();
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public Map<String, Object> count(@RequestParam(defaultValue = "30") long windowMinutes)
    {
        long clampedMinutes = Math.max(1, Math.min(windowMinutes, 24 * 60));
        Duration window = Duration.ofMinutes(clampedMinutes);

        OnlineUserService.OnlineCount count = service.getCount(window, Instant.now());

        return Map.of(
                "windowMinutes", window.toMinutes(),
                "guests", count.guests(),
                "registered", count.registered(),
                "total", count.total()
        );
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
}

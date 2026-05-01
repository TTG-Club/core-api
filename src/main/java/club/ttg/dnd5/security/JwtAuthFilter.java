package club.ttg.dnd5.security;

import club.ttg.dnd5.domain.user.model.Role;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.exception.ApiException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtils jwtService;

    public static final String BEARER_PREFIX = "Bearer ";

    public static final String HEADER_NAME = "Authorization";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ApiException, ServletException, IOException {
        String authHeader = request.getHeader(HEADER_NAME);

        if (isInvalidAuthHeader(authHeader)) {
            filterChain.doFilter(request, response);

            return;
        }

        String jwt = authHeader.substring(BEARER_PREFIX.length());

        try {
            if (isInvalidToken(jwt, response)) {
                filterChain.doFilter(request, response);

                return;
            }
        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            resetTokenCookie(response);
            filterChain.doFilter(request, response);

            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);

            return;
        }

        if (jwtService.isTokenValid(jwt)) {
            authenticateUser(jwt, request);
        } else {
            resetTokenCookie(response);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isInvalidAuthHeader(String authHeader) {
        return authHeader == null || !authHeader.startsWith(BEARER_PREFIX);
    }

    private boolean isInvalidToken(String jwt, HttpServletResponse response) {
        if (jwtService.isTokenExpired(jwt)) {
            resetTokenCookie(response);

            return true;
        }

        String username = jwtService.extractUsername(jwt);

        if (!StringUtils.hasLength(username)) {
            resetTokenCookie(response);

            return true;
        }

        return false;
    }

    private void authenticateUser(String jwt, HttpServletRequest request) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        User user = buildUser(jwt);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);
    }

    private User buildUser(String jwt) {
        User user = new User();
        String userId = jwtService.extractUserId(jwt);

        if (StringUtils.hasLength(userId)) {
            try {
                user.setUuid(UUID.fromString(userId));
            } catch (IllegalArgumentException ignored) {
                // Old core-api tokens used username as subject. They are still allowed to expire naturally.
            }
        }

        user.setUsername(jwtService.extractUsername(jwt));
        user.setEmail(jwtService.extractEmail(jwt));
        user.setEnabled(true);
        user.setRoles(jwtService.extractRoles(jwt).stream()
                .map(role -> Role.builder().name(role).build())
                .toList());

        return user;
    }

    private void resetTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("ttg-user-token", "");
        cookie.setMaxAge(-1);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}

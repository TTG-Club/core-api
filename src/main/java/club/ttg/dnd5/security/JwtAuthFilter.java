package club.ttg.dnd5.security;

import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.model.user.User;
import club.ttg.dnd5.service.user.UserService;
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

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtils jwtService;

    private final UserService userService;

    public static final String BEARER_PREFIX = "Bearer ";

    public static final String HEADER_NAME = "Authorization";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ApiException, ServletException, IOException {

        String authHeader = request.getHeader(HEADER_NAME);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);

            return;
        }

        String jwt = authHeader.substring(BEARER_PREFIX.length());

        if (jwtService.isTokenExpired(jwt)) {
            response.addCookie(getResetTokenCookie());
            filterChain.doFilter(request, response);

            return;
        }

        String username = jwtService.extractUsername(jwt);

        if (!StringUtils.hasLength(username)) {
            response.addCookie(getResetTokenCookie());
            filterChain.doFilter(request, response);

            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);

            return;
        }


        if (jwtService.isTokenValid(jwt)) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            User user = userService.getByUsername(username);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getAuthorities()
            );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);
        } else {
            response.addCookie(getResetTokenCookie());
        }

        filterChain.doFilter(request, response);
    }

    private Cookie getResetTokenCookie() {
        Cookie cookie = new Cookie("ttg-user-token", "");

        cookie.setMaxAge(-1);
        cookie.setPath("/");

        return cookie;
    }
}

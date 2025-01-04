package club.ttg.dnd5.security;

import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.model.user.User;
import club.ttg.dnd5.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
        String username = jwtService.extractUsername(jwt);

        if (!StringUtils.hasLength(username)) {
            filterChain.doFilter(request, response);

            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);

            return;
        }

        try {
            User user = userService.getByUsername(username);

            if (jwtService.isTokenValid(jwt, user)) {
                SecurityContext context = SecurityContextHolder.createEmptyContext();

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            }

            filterChain.doFilter(request, response);
        } catch (ApiException e) {
            logger.error(e);

            ApiException errorResponse = new ApiException(e.getStatus(), e.getMessage());

            response.setStatus(e.getStatus().value());
            response.getWriter().write(errorResponse.toString());
        }
    }
}

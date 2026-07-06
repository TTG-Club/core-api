package club.ttg.dnd5.security;

import club.ttg.dnd5.config.properties.InternalServiceProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Защищает внутренние ручки {@code /api/internal/**} общим секретом сервисов
 * вместо JWT: входящий заголовок {@value #SERVICE_TOKEN_HEADER} должен совпасть
 * с настроенным секретом ({@code internal.service-secret}).
 * <p>
 * Fail-closed: если секрет не сконфигурирован (пустой) или заголовок не совпадает —
 * запрос отклоняется с 401. Остальные пути фильтр не трогает ({@link #shouldNotFilter}),
 * их обслуживает обычная цепочка с {@link JwtAuthFilter}.
 */
@Component
@RequiredArgsConstructor
public class InternalServiceTokenFilter extends OncePerRequestFilter {
    public static final String SERVICE_TOKEN_HEADER = "X-Service-Token";

    private static final String INTERNAL_PATH_PREFIX = "/api/internal/";

    private final InternalServiceProperties properties;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String configuredSecret = properties.getServiceSecret();
        String providedToken = request.getHeader(SERVICE_TOKEN_HEADER);

        if (!StringUtils.hasText(configuredSecret) || !configuredSecret.equals(providedToken)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !request.getRequestURI().startsWith(INTERNAL_PATH_PREFIX);
    }
}

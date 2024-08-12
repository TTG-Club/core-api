package club.ttg.dnd5.service.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;

    private final UserDetailsService userDetailsService;

    /**
     * Performs the authentication process for each incoming request.
     * Extracts the JWT token from the Authorization header, validates it, and sets the authentication
     * in the security context if the token is valid.
     *
     * @param httpServletRequest  the incoming HTTP request
     * @param httpServletResponse the outgoing HTTP response
     * @param filterChain         the filter chain to continue processing the request
     * @throws ServletException if there is an error during the filter processing
     * @throws IOException      if there is an error with the input/output of the filter
     */
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest httpServletRequest,
                                    @NonNull HttpServletResponse httpServletResponse,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = httpServletRequest.getHeader("Authorization"); //here JWT token
        String jwt;
        String userEmail = "";
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        jwt = authHeader.substring(7); // 7 because word -  bearer
        try {
            userEmail = jwtService.extractUsername(jwt);
            LOGGER.info("User email extracted from JWT: {}", userEmail);

        } catch (ExpiredJwtException e) {
            LOGGER.info("JWT token is expired, generating new token from refresh token.");
            jwt = jwtService.generateAccessTokenFromRefresh(jwt);
            userEmail = jwtService.extractUsername(jwt);
        }
        httpServletResponse.setHeader("X-Access-Token", jwt);
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(httpServletRequest)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                LOGGER.info("User successfully authenticated.");
            } else {
                LOGGER.info("JWT token is not valid for user {}.", userEmail);
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
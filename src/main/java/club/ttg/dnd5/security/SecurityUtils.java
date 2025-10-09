package club.ttg.dnd5.security;

import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

/**
 * Класс утилита для работы с аутентифицированным пользователем
 */
public final class SecurityUtils {
    private SecurityUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Метод возвращает данные пользователя расшифрованные из токена.
     *
     * @return Данные пользователя.
     */
    public static User getUser() throws ApiException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (Objects.isNull(authentication)) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
            }

            return (User) authentication.getPrincipal();
        } catch (InsufficientAuthenticationException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
    }

    public static String getUsername() throws ApiException {
        return getUser().getUsername();
    }
}

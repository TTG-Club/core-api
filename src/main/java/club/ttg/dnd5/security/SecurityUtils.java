package club.ttg.dnd5.security;

import club.ttg.dnd5.dto.user.UserDto;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.model.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;
import java.util.stream.Collectors;

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

    /**
     * Получение объекта с данными пользователя, получаемые при авторизации
     *
     * @return Объект с логином, email, и ролями.
     */
    public static UserDto getUserDto() throws ApiException {
        return convertUserToUserDto(getUser());
    }

    /**
     * Конвертирует User в UserDto
     *
     * @param user Сущность пользователя
     * @return UserDto
     */
    public static UserDto convertUserToUserDto(User user) {
        return UserDto.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter(role -> !role.isBlank())
                        .collect(Collectors.toList()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

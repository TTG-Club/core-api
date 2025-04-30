package club.ttg.dnd5.domain.user.service;

import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.repository.UserRepository;
import club.ttg.dnd5.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getByUsername(String username) throws ApiException {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
    }

    public User getByUsernameOrEmail(String usernameOrEmail) throws ApiException {
        return userRepository.findByEmailOrUsername(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
    }

    public Optional<UUID> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof User user) {
            return Optional.of(user.getUuid());
        }
        return Optional.empty();
    }

    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }
}

package club.ttg.dnd5.domain.user.service;

import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

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

    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }
}

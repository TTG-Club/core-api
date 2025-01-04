package club.ttg.dnd5.service;

import club.ttg.dnd5.dto.user.SignUpDto;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.model.user.Role;
import club.ttg.dnd5.model.user.User;
import club.ttg.dnd5.repository.user.RoleRepository;
import club.ttg.dnd5.repository.user.UserRepository;
import club.ttg.dnd5.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtils jwtUtils;

    private final UserService userService;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final AuthenticationManager authenticationManager;

    public String signIn(String usernameOrEmail, String password, Boolean remember) {
        try {
            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(usernameOrEmail, password, new ArrayList<>()));
        } catch (BadCredentialsException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Неверное имя пользователя или пароль");
        } catch (DisabledException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Учетная запись не активирована");
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Неизвестная ошибка");
        }

        User user = userService.getByUsernameOrEmail(usernameOrEmail);
        long expiration = jwtUtils.getExpirationInMilliseconds(remember);

        return jwtUtils.generateToken(user, expiration);
    }

    public void signUp(SignUpDto signUpDto) throws ApiException {
        if (userRepository.existsByUsername(signUpDto.getUsername())) {
            throw new ApiException(HttpStatus.CONFLICT, "Пользователь с таким именем пользователя уже существует");
        }

        if (userRepository.existsByEmail(signUpDto.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Пользователь с таким e-mail уже существует");
        }

        User user = new User();

        user.setEmail(signUpDto.getEmail());
        user.setUsername(signUpDto.getUsername());
        user.setPassword(new BCryptPasswordEncoder().encode(signUpDto.getPassword()));

        Role role = roleRepository.findByName("USER");

        user.setRoles(Collections.singletonList(role));

        userRepository.save(user);
    }
}

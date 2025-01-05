package club.ttg.dnd5.service;

import club.ttg.dnd5.dto.user.SignUpDto;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.model.user.OneTimeToken;
import club.ttg.dnd5.model.user.Role;
import club.ttg.dnd5.model.user.User;
import club.ttg.dnd5.repository.user.OneTimeTokenRepository;
import club.ttg.dnd5.repository.user.RoleRepository;
import club.ttg.dnd5.repository.user.UserRepository;
import club.ttg.dnd5.security.JwtUtils;
import io.jsonwebtoken.lang.Strings;
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
import java.util.UUID;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final OneTimeTokenRepository oneTimeTokenRepository;

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

    public void signUp(SignUpDto signUpDto) {
        isUserNotExist(signUpDto.getUsername(), signUpDto.getEmail());

        User user = new User();

        user.setEmail(signUpDto.getEmail());
        user.setUsername(signUpDto.getUsername());
        user.setPassword(new BCryptPasswordEncoder().encode(signUpDto.getPassword()));

        Role role = roleRepository.findByName("USER");

        user.setRoles(Collections.singletonList(role));

        userRepository.save(user);
        emailService.confirmEmail(user);
    }

    public void isUserNotExist(String username, String email) throws ApiException {
        if (!Strings.hasLength(username) && !Strings.hasLength(email)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Необходимо указать имя пользователя или электронный адрес");
        }

        if (userRepository.existsByUsername(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "Пользователь с таким именем уже существует");
        }

        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Пользователь с таким e-mail уже существует");
        }
    }

    public void confirmEmail(UUID token) throws ApiException {
        OneTimeToken oneTimeToken = oneTimeTokenRepository.findById(token)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Срок действия ссылки истек или она уже была использована"));

        User user = oneTimeToken.getUser();

        if (user.isEnabled()) {
            throw new ApiException(HttpStatus.NOT_MODIFIED, "Ваш электронный адрес уже подтвержден");
        }

        user.setEnabled(true);

        userRepository.save(user);
        oneTimeTokenRepository.delete(oneTimeToken);
    }

}

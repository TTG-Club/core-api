package club.ttg.dnd5.domain.user.rest.controller;

import club.ttg.dnd5.domain.user.rest.dto.SignInDto;
import club.ttg.dnd5.domain.user.rest.dto.SignUpDto;
import club.ttg.dnd5.security.JwtUtils;
import club.ttg.dnd5.domain.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@Tag(name = "Аутентификация пользователя")

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtUtils jwtUtils;

    private final AuthService authService;

    @Operation(summary = "Аутентификация")
    @PostMapping("/sign-in")
    public void signIn(@RequestBody SignInDto request, HttpServletResponse response) {
        String jwt = authService.signIn(request.getUsernameOrEmail(), request.getPassword(), request.isRemember());
        long expiration = jwtUtils.extractExpiration(jwt).getTime();
        long maxAge = (expiration - Instant.now().toEpochMilli()) / 1000;

        Cookie cookie = new Cookie("ttg-user-token", jwt);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) maxAge);

        response.addCookie(cookie);
    }

    @Operation(summary = "Регистрация пользователя")
    @PostMapping("/sign-up")
    public void signUp(@RequestBody SignUpDto user) {
        authService.signUp(user);
    }

    @Operation(summary = "Выход пользователя")
    @GetMapping("/logout")
    public void logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();

        Cookie cookie = new Cookie("ttg-user-token", "");
        cookie.setMaxAge(-1);
        cookie.setPath("/");

        response.addCookie(cookie);
    }

    @Operation(summary = "Проверка существования пользователя", description = "Проверяет существование пользователя по e-mail или логину")
    @GetMapping("/exist")
    public void isUserNotExist(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email
    ) {
        authService.isUserNotExist(username, email);
    }

    @Operation(summary = "Подтверждение почты")
    @PutMapping("/confirm/email")
    public void confirmEmail(@RequestParam UUID token) {
        authService.confirmEmail(token);
    }
}

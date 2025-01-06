package club.ttg.dnd5.controller;

import club.ttg.dnd5.dto.user.SignInDto;
import club.ttg.dnd5.dto.user.SignUpDto;
import club.ttg.dnd5.security.JwtUtils;
import club.ttg.dnd5.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtils jwtUtils;

    private final AuthService authService;

    @PostMapping("/sign-in")
    public void signIn(@RequestBody SignInDto request, HttpServletResponse response) {
        String jwt = authService.signIn(request.getUsernameOrEmail(), request.getPassword(), request.isRemember());
        long expiration = jwtUtils.extractExpiration(jwt).getTime() / 1000;

        Cookie cookie = new Cookie("ttg-user-token", jwt);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) expiration);

        response.addCookie(cookie);
    }

    @PostMapping("/sign-up")
    public void signUp(@RequestBody SignUpDto user) {
        authService.signUp(user);
    }

    @Operation(summary = "Log out user current session")
    @GetMapping("/logout")
    public void logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();

        Cookie cookie = new Cookie("ttg-user-token", "");
        cookie.setMaxAge(-1);
        cookie.setPath("/");

        response.addCookie(cookie);
    }

    @Operation(summary = "Nickname and mailing address check")
    @GetMapping("/exist")
    public void isUserNotExist(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email
    ) {
        authService.isUserNotExist(username, email);
    }

    @PutMapping("/confirm/email")
    public void confirmEmail(@RequestParam UUID token) {
        authService.confirmEmail(token);
    }

}

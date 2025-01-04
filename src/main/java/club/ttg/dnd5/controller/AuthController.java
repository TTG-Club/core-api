package club.ttg.dnd5.controller;

import club.ttg.dnd5.dto.user.SignInDto;
import club.ttg.dnd5.dto.user.SignUpDto;
import club.ttg.dnd5.security.JwtUtils;
import club.ttg.dnd5.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v2/auth")
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

}

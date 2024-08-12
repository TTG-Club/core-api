package club.ttg.dnd5.service.security;

import club.ttg.dnd5.dto.security.LoginUserRequest;
import club.ttg.dnd5.dto.security.RegisterUserRequest;
import club.ttg.dnd5.dto.security.RegisterUserResponse;
import club.ttg.dnd5.exceptions.EmailNotFoundException;
import club.ttg.dnd5.model.user.User;
import club.ttg.dnd5.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager manager;

    //refactor to DTO
    public RegisterUserResponse signup(RegisterUserRequest input) {
        User user = User.builder()
                .name(input.getFullName())
                .email(input.getEmail())
                .password(passwordEncoder.encode(input.getPassword()))
                .build();
        userCredentialRepository.save(user);
        return new RegisterUserResponse(input.getFullName());
    }

    //refactor to DTO
    public RegisterUserResponse authenticate(LoginUserRequest input, HttpHeaders headers) {
        manager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );
        var user = userCredentialRepository.findByEmail(input.getEmail()).orElseThrow(() -> new EmailNotFoundException("Email not found"));
        var refreshToken = jwtService.generateToken(user);
        refreshTokenService.createRefreshToken(user, refreshToken);
        var jwt = jwtService.generateAccessTokenFromRefresh(refreshToken);
        headers.add("X-Access-Token", jwt);
        return new RegisterUserResponse (user.getName());
    }

    public String signOut(String email, HttpHeaders headers) {
        var user = userCredentialRepository
                .findByEmail(email).orElseThrow(() -> new EmailNotFoundException("Email not found"));
        refreshTokenService.removeRefreshTokenBasedOnAccessToken(user);
        headers.remove("X-Access-Token");
        return "Sign out was successfully";
    }

}
package club.ttg.dnd5.controller.security;

import club.ttg.dnd5.dto.security.LoginUserRequest;
import club.ttg.dnd5.dto.security.RegisterUserRequest;
import club.ttg.dnd5.dto.security.RegisterUserResponse;
import club.ttg.dnd5.service.security.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    /**
     * Registers a new user.
     *
     * @param request The registration request containing user details.
     * @return ResponseEntity containing the authentication response with registration results.
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterUserRequest request) {
        // Call the signup service method and get the response message
        String responseMessage = service.signup(request);
        // Return the response as plain text
        return ResponseEntity.ok(responseMessage);
    }

    /**
     * Authenticates a user.
     *
     * @param request The authentication request containing user credentials.
     * @return ResponseEntity containing the authentication response with authentication results.
     */
    @PostMapping("/authenticate")
    public ResponseEntity<RegisterUserResponse> authenticate(@RequestBody LoginUserRequest request) {
        HttpHeaders headers = new HttpHeaders();
        RegisterUserResponse authenticationResponse = service.authenticate(request, headers);
        return ResponseEntity.ok().headers(headers).body(authenticationResponse);
    }

    @DeleteMapping("/sign-out")
    public ResponseEntity<String> signOut(@RequestParam String email, @RequestHeader HttpHeaders headers) {
        String message = service.signOut(email, headers);
        return ResponseEntity.ok(message);
    }
}

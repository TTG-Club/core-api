package club.ttg.dnd5.config;

import club.ttg.dnd5.exceptions.EmailNotFoundException;
import club.ttg.dnd5.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class JWTConfig {
    private final UserRepository userCredentialRepository;

    /**
     * Creates and configures the UserDetailsService bean.
     *
     * @return The UserDetailsService bean implementation.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userCredentialRepository.findByEmail(username).
                orElseThrow(() -> new EmailNotFoundException("We could not find an account associated with this email address. Please check your email address or sign up for a new account."));
    }

    /**
     * Creates and configures the AuthenticationProvider bean.
     *
     * @return The AuthenticationProvider bean implementation.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Creates and configures the PasswordEncoder bean.
     *
     * @return The PasswordEncoder bean implementation.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates and configures the AuthenticationManager bean.
     *
     * @param configuration The AuthenticationConfiguration object.
     * @return The AuthenticationManager bean.
     * @throws Exception If an error occurs during configuration.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
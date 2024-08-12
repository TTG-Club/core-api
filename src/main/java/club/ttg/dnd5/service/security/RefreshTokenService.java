package club.ttg.dnd5.service.security;

import club.ttg.dnd5.exceptions.RefreshTokenException;
import club.ttg.dnd5.model.security.RefreshToken;
import club.ttg.dnd5.model.user.User;
import club.ttg.dnd5.repository.security.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

/**
 * Service class for managing refresh tokens.
 * Provides operations for creating, finding, and verifying refresh tokens.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Retrieves a RefreshToken entity based on the provided token.
     *
     * @param token the token value to search for
     * @return the RefreshToken entity, or null if not found
     */
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token).orElse(null);
    }

    /**
     * Creates a new refresh token for the provided UserCredential entity.
     *
     * @param userCredential the UserCredential entity to createGroup the refresh token for
     * @param newToken       the new token value for the refresh token
     */
    public void createRefreshToken(User userCredential, String newToken) {
        ZonedDateTime expiration = ZonedDateTime.now().plusHours(7);
        // Check if there is an existing refresh token for the user
        RefreshToken existingToken = refreshTokenRepository.findByUserCredential(userCredential).orElse(null);
        if (existingToken != null) {
            refreshTokenRepository.delete(existingToken);
        }
        RefreshToken refreshToken = RefreshToken.builder()
                .token(newToken)
                .expiration(expiration)
                .userCredential(userCredential)
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Verifies if the provided refresh token has expired.
     * If the token has expired, it is deleted from the repository and an exception is thrown.
     *
     * @param token the RefreshToken entity to verify
     * @return true if the token has expired and was deleted, false otherwise
     * @throws RefreshTokenException if the token has expired
     */
    public boolean verifyExpiration(RefreshToken token) {
        if (token.getExpiration().compareTo(ZonedDateTime.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenException("Refresh token was expired. Please make a new sign in request");
        }
        return false;
    }

    public void removeRefreshTokenBasedOnAccessToken(User userCredential) {
        refreshTokenRepository.deleteByUserCredential(userCredential);
    }
}


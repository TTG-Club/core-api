package club.ttg.dnd5.repository.security;

import club.ttg.dnd5.model.security.RefreshToken;
import club.ttg.dnd5.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    /**
     * Retrieves an optional RefreshToken entity based on the provided token.
     *
     * @param token the token value to search for
     * @return an Optional containing the RefreshToken entity, or empty if not found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Deletes the RefreshToken associated with the provided UserCredential entity.
     *
     * @param userCredential the UserCredential entity to delete the RefreshToken for
     */
    void deleteByUserCredential(User userCredential);

    /**
     * Retrieves an optional RefreshToken entity based on the provided UserCredential entity.
     *
     * @param userCredential the UserCredential entity to search for
     * @return an Optional containing the RefreshToken entity, or empty if not found
     */
    Optional<RefreshToken> findByUserCredential(User userCredential);

}

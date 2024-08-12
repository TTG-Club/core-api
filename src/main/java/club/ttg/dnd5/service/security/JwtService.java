package club.ttg.dnd5.service.security;

import club.ttg.dnd5.exceptions.AccessTokenException;
import club.ttg.dnd5.exceptions.RefreshTokenException;
import club.ttg.dnd5.model.security.RefreshToken;
import club.ttg.dnd5.model.user.User;
import club.ttg.dnd5.repository.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtService.class);
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userCredentialRepository;
    private static final String SECRET_KEY = "6E5A7234753778214125432A462D4A614E645267556B58703273357638792F42";

    /**
     * Extracts the username from the provided JWT token.
     *
     * @param token the JWT token
     * @return the username extracted from the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the claim value from the provided JWT token using the given claims resolver function.
     *
     * @param token          the JWT token
     * @param claimsResolver the claims resolver function
     * @param <T>            the type of the claim value
     * @return the claim value extracted from the token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from the provided JWT token.
     *
     * @param token the JWT token
     * @return the claims extracted from the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Generates a new JWT token for the provided user details.
     *
     * @param userDetails the user details
     * @return the generated JWT token
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a new JWT token for the provided user details and additional claims.
     *
     * @param extractClaims the additional claims to include in the token
     * @param userDetails   the user details
     * @return the generated JWT token
     */
    public String generateToken(
            Map<String, Object> extractClaims,
            UserDetails userDetails
    ) {
        LOGGER.info("Generating token for user {}", userDetails.getUsername());
        return Jwts
                .builder()
                .setClaims(extractClaims)
//                .setPayload()
                .setSubject(userDetails.getUsername()) //here this email (but for Spring this is username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24)) // how Long JWT token should be valid (System.currentTimeMillis() + 1000 * 60 * 24)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateTokenWhenRefreshExist(
            Map<String, Object> extractClaims,
            UserDetails userDetails,
            String refreshToken
    ) {
        LOGGER.info("Generating token for user {}", userDetails.getUsername());
        return Jwts
                .builder()
                .claim("refresh_token", refreshToken)
                .addClaims(extractClaims)
                .setSubject(userDetails.getUsername()) //here this email (but for Spring this is username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                //15 min
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15)) // how Long JWT token should be valid (System.currentTimeMillis() + 1000 * 60 * 24)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generates a new access token from a refresh token.
     *
     * @param token the refresh token
     * @return the generated access token
     * @throws AccessTokenException  if the access token doesn't exist
     * @throws RefreshTokenException if the refresh token has expired
     */
    public String generateAccessTokenFromRefresh(String token) {
        RefreshToken refreshToken = refreshTokenService.findByToken(token);
        if (refreshToken == null) {
            throw new AccessTokenException("Access token doesnt exist");
        }
        User userCredential = refreshToken.getUserCredential();
        if (!refreshTokenService.verifyExpiration(refreshToken)) {
            String accessToken = generateTokenWhenRefreshExist(new HashMap<>(), userCredential, refreshToken.getToken());
//            refreshToken.setToken(accessToken);
//            refreshTokenRepository.save(refreshToken);
            LOGGER.info("Generated new access token from refresh token for user: {}", userCredential.getEmail());
            return accessToken;
        } else {
            throw new RefreshTokenException("Refresh token was expired. Please make a new sign in request");
        }
    }

    public String generateAccessTokenByEmail(String email) {
        User userCredential = userCredentialRepository.findByEmail(email).get();
        if (userCredential == null) {
            throw new AccessTokenException("User with the provided email doesn't exist");
        }

        RefreshToken refreshToken = userCredential.getRefreshToken(); // Assuming a relationship exists
        if (refreshToken == null) {
            throw new AccessTokenException("No refresh token found for the user");
        }

        if (!refreshTokenService.verifyExpiration(refreshToken)) {
            String accessToken = generateTokenWhenRefreshExist(new HashMap<>(), userCredential, refreshToken.getToken());
            LOGGER.info("Generated new access token from refresh token for user: {}", userCredential.getEmail());
            return accessToken;
        } else {
            throw new RefreshTokenException("Refresh token was expired. Please make a new sign in request");
        }
    }

    /**
     * Verifies if the provided token is valid for the given user details.
     *
     * @param token       the JWT token to verify
     * @param userDetails the user details
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = extractUsername(token);
        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Checks if the provided JWT token has expired.
     *
     * @param token the JWT token to check
     * @return true if the token has expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from the provided JWT token.
     *
     * @param token the JWT token
     * @return the expiration date extracted from the token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Retrieves the signing key used for JWT token verification.
     *
     * @return the signing key
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

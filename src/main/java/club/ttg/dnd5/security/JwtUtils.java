package club.ttg.dnd5.security;

import club.ttg.dnd5.model.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtils {
    @Value("${token.api.secret}")
    private String SECRET_KEY;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(User user) {
        return createToken(user, getExpirationInMilliseconds(Boolean.FALSE));
    }

    public String generateToken(User user, long expiration) {
        return createToken(user, expiration);
    }

    public Boolean isTokenValid(String token, User user) {
        final String username = extractUsername(token);

        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }

    private String createToken(User user, long expiration) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("roles", user.getRoles());
        claims.put("username", user.getUsername());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(this.getSigningKey())
                .compact();
    }

    public long getExpirationInMilliseconds(Boolean remember) {
        long expiration = 24 * 60 * 60 * 1000L;

        if (remember != null && remember) {
            expiration = expiration * 30;
        }

        return expiration;
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(this.getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}

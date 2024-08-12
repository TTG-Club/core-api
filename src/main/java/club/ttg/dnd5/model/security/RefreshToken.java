package club.ttg.dnd5.model.security;

import club.ttg.dnd5.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@EqualsAndHashCode(of = "uuid")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_token")
public class RefreshToken {

    /**
     * The unique identifier for the refresh token.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The UUID of the refresh token.
     */
    @Builder.Default
    private String uuid = UUID.randomUUID().toString();

    /**
     * The token string.
     */
    @Column(nullable = false, unique = true, length = 2000)
    private String token;

    /**
     * The expiration date and time of the refresh token.
     */
    @Column(nullable = false)
    private ZonedDateTime expiration;

    /**
     * The user credential associated with the refresh token.
     */
    @OneToOne
    @JoinColumn(nullable = false, name = "users_id")
    private User userCredential;

    /**
     * Returns a string representation of the refresh token.
     *
     * @return A string representation of the refresh token.
     */
    @Override
    public String toString() {
        return "RefreshToken{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", token='" + token + '\'' +
                ", expiration=" + expiration +
                '}';
    }
}

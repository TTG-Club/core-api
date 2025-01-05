package club.ttg.dnd5.model.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "one_time_tokens")
public class OneTimeToken {

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_uuid")
    private User user;

    private Instant expiryDate;

    public OneTimeToken(User user) {
        this.user = user;
        this.expiryDate = Date.from(Instant.now().plus(Duration.ofDays(1))).toInstant();
    }
}

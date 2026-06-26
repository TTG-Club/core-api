package club.ttg.dnd5.domain.achievement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Достижение, выданное пользователю. Уникально по паре (пользователь, достижение) —
 * повторная выдача не дублируется.
 */
@Getter
@Setter
@Entity
@Table(name = "user_achievement")
public class UserAchievement {
    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Column(name = "username", nullable = false)
    private String username;

    /** Код достижения из каталога {@link Achievement}. */
    @Column(name = "achievement_code", nullable = false, length = 64)
    private String achievementCode;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt;

    /** Код погашения, по которому выдано (null для ручной/авто выдачи). */
    @Column(name = "source_code")
    private UUID sourceCode;

    /** Логин админа при ручной выдаче (null для авто/по коду). */
    @Column(name = "granted_by")
    private String grantedBy;
}

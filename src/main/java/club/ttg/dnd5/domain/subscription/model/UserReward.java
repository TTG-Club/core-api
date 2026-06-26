package club.ttg.dnd5.domain.subscription.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Постоянная награда, закреплённая за пользователем. Уникальна по паре
 * (пользователь, перк) — повторное погашение не дублирует награду.
 */
@Getter
@Setter
@Entity
@Table(name = "user_reward")
public class UserReward {
    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Column(name = "username", nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "perk", nullable = false, length = 48)
    private RewardPerk perk;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt;

    /** Код, по которому выдана награда (null для ручной выдачи). */
    @Column(name = "source_code")
    private UUID sourceCode;
}

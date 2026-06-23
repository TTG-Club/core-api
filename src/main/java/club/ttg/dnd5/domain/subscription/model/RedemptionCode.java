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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Одноразовый код, который выпускает админ. При погашении выдаёт награды и/или
 * создаёт подписку. Код может нести только подписку, только награды или то и другое.
 */
@Getter
@Setter
@Entity
@Table(name = "redemption_code")
public class RedemptionCode {
    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type", length = 32)
    private SubscriptionType subscriptionType;

    @Column(name = "subscription_months")
    private Integer subscriptionMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_tier", length = 32)
    private RewardTier rewardTier;

    /** Произвольная пометка админа (например, «Kickstarter, тир 4»). */
    @Column(name = "label")
    private String label;

    @Column(name = "redeemed_by")
    private String redeemedBy;

    @Column(name = "redeemed_at")
    private Instant redeemedAt;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp(source = SourceType.DB)
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp(source = SourceType.DB)
    private Instant updatedAt;
}

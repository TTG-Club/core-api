package club.ttg.dnd5.domain.subscription.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
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

    /** Готовый пресет перков (кумулятивный тир). Объединяется с {@link #perks} при погашении. */
    @Enumerated(EnumType.STRING)
    @Column(name = "reward_tier", length = 32)
    private RewardTier rewardTier;

    /** Произвольный набор косметических перков, привязанных к коду помимо тира. */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "redemption_code_perk", joinColumns = @JoinColumn(name = "code_uuid"))
    @Enumerated(EnumType.STRING)
    @Column(name = "perk", length = 48)
    private Set<RewardPerk> perks = new HashSet<>();

    /** Произвольный набор кодов достижений, привязанных к коду. */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "redemption_code_achievement", joinColumns = @JoinColumn(name = "code_uuid"))
    @Column(name = "achievement_code", length = 64)
    private Set<String> achievements = new HashSet<>();

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

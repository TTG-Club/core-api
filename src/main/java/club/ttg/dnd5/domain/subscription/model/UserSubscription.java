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

@Getter
@Setter
@Entity
@Table(name = "user_subscription")
public class UserSubscription {
    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SubscriptionType type;

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;

    @Column(name = "owner_username")
    private String ownerUsername;

    @Column(name = "registered_at")
    private Instant registeredAt;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp(source = SourceType.DB)
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp(source = SourceType.DB)
    private Instant updatedAt;
}

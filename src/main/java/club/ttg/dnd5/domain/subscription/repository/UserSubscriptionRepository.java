package club.ttg.dnd5.domain.subscription.repository;

import club.ttg.dnd5.domain.subscription.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UUID> {
    Optional<UserSubscription> findByRegistrationCode(String registrationCode);

    boolean existsByRegistrationCode(String registrationCode);

    List<UserSubscription> findByOwnerUsernameOrderByCreatedAtDesc(String ownerUsername);

    boolean existsByOwnerUsername(String ownerUsername);

    boolean existsByOwnerUsernameAndStartsAtIsNotNullAndExpiresAtAfter(String ownerUsername, Instant now);
}

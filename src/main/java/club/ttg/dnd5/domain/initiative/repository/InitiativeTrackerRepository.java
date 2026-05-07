package club.ttg.dnd5.domain.initiative.repository;

import club.ttg.dnd5.domain.initiative.model.InitiativeTracker;
import club.ttg.dnd5.domain.initiative.model.InitiativeTrackerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InitiativeTrackerRepository extends JpaRepository<InitiativeTracker, UUID> {
    Optional<InitiativeTracker> findFirstByOwnerIdAndStatusNotOrderByUpdatedAtDesc(UUID ownerId, InitiativeTrackerStatus status);

    Optional<InitiativeTracker> findByIdAndOwnerId(UUID id, UUID ownerId);

    Optional<InitiativeTracker> findByShareToken(String shareToken);
}

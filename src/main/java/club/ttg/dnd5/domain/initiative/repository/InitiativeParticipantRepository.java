package club.ttg.dnd5.domain.initiative.repository;

import club.ttg.dnd5.domain.initiative.model.InitiativeParticipant;
import club.ttg.dnd5.domain.initiative.model.InitiativeTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InitiativeParticipantRepository extends JpaRepository<InitiativeParticipant, UUID> {
    List<InitiativeParticipant> findAllByTrackerOrderByOrderIndexAsc(InitiativeTracker tracker);

    int countByTrackerAndSourceCreatureUrl(InitiativeTracker tracker, String sourceCreatureUrl);

    void deleteByIdAndTracker(UUID id, InitiativeTracker tracker);
}

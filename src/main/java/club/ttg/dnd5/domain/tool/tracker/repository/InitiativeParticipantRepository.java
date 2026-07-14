package club.ttg.dnd5.domain.tool.tracker.repository;

import club.ttg.dnd5.domain.tool.tracker.model.InitiativeParticipant;
import club.ttg.dnd5.domain.tool.tracker.model.ParticipantType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InitiativeParticipantRepository extends JpaRepository<InitiativeParticipant, UUID> {

    List<InitiativeParticipant> findAllByTrackerId(UUID trackerId);

    Optional<InitiativeParticipant> findByIdAndTrackerId(UUID id, UUID trackerId);

    long countByTrackerIdAndType(UUID trackerId, ParticipantType type);

    @Query("SELECT COALESCE(MAX(p.seq), 0) FROM InitiativeParticipant p WHERE p.trackerId = :trackerId")
    int findMaxSeq(@Param("trackerId") UUID trackerId);

    /**
     * Физически удаляет всех участников трекера — при мягком удалении трекера с владельцем:
     * в истории остаётся только строка трекера, участники недоступны и не должны копиться.
     */
    @Modifying
    @Query("DELETE FROM InitiativeParticipant p WHERE p.trackerId = :trackerId")
    void deleteAllByTrackerId(@Param("trackerId") UUID trackerId);
}

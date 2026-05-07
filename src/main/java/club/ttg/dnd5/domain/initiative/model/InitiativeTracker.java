package club.ttg.dnd5.domain.initiative.model;

import club.ttg.dnd5.domain.common.model.Timestamped;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "initiative_tracker", indexes = {
        @Index(name = "initiative_tracker_owner_idx", columnList = "owner_id"),
        @Index(name = "initiative_tracker_share_token_idx", columnList = "share_token", unique = true)
})
public class InitiativeTracker extends Timestamped {
    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private String title = "Initiative Tracker";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InitiativeTrackerStatus status = InitiativeTrackerStatus.SETUP;

    @Column(nullable = false)
    private int currentRound = 1;

    @Column(name = "current_participant_id")
    private UUID currentParticipantId;

    @Column(nullable = false)
    private boolean rerollEachRound;

    @Column(nullable = false)
    private boolean groupSameCreaturesInitiative;

    @Column(name = "share_token", unique = true)
    private String shareToken;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private EncounterDifficulty encounterDifficulty;
}

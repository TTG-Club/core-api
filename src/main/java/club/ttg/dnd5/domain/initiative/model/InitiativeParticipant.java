package club.ttg.dnd5.domain.initiative.model;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.common.model.Timestamped;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "initiative_participant", indexes = {
        @Index(name = "initiative_participant_tracker_idx", columnList = "tracker_id"),
        @Index(name = "initiative_participant_source_creature_idx", columnList = "source_creature_id")
})
public class InitiativeParticipant extends Timestamped {
    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tracker_id", nullable = false)
    private InitiativeTracker tracker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InitiativeParticipantType type = InitiativeParticipantType.PLAYER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InitiativeRelationType relationType = InitiativeRelationType.ALLY;

    @Column(nullable = false)
    private String name;

    private String baseName;
    private String displayName;

    private Integer level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_creature_id")
    private Creature sourceCreature;

    private Integer sameCreatureIndex;

    @Column(nullable = false)
    private int hpMax;

    @Column(nullable = false)
    private int hpCurrent;

    @Column(nullable = false)
    private int hpTemporary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InitiativeParticipantState state = InitiativeParticipantState.ACTIVE;

    @Column(nullable = false)
    private int initiativeBonus;

    @Column(nullable = false)
    private int dexterityBonus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InitiativeRollMode rollMode = InitiativeRollMode.MANUAL;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<Integer> rolls = new ArrayList<>();

    private Integer rollValue;
    private Integer initiativeTotal;

    @Column(nullable = false)
    private int orderIndex;

    @Column(nullable = false)
    private int addedRound = 1;
}

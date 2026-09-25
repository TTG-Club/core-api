package club.ttg.dnd5.domain.bastion.player.activity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Запись журнала: ход бастиона — 7 игровых дней. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "player_bastion_turn",
        indexes = @Index(name = "player_bastion_turn_bastion_id_index", columnList = "bastion_id"))
public class PlayerBastionTurn {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bastion_id", nullable = false)
    private UUID bastionId;

    /** Номер хода, который наступил. */
    @Column(nullable = false)
    private int number;

    /** В этот ход бастион обслуживали: приказ «Обслуживать» или приказов не было. */
    @Column(nullable = false)
    private boolean maintain;

    /** Событие бастиона, которое выпало мастеру при обслуживании. */
    @Column(columnDefinition = "TEXT")
    private String event;

    /** Что произошло за ход: достроено, завершены приказы. */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> summary = new ArrayList<>();

    @Column(name = "performed_by", nullable = false)
    private UUID performedBy;

    @Column(name = "performed_at", nullable = false, updatable = false)
    private Instant performedAt;

    @PrePersist
    void prePersist() {
        if (performedAt == null) {
            performedAt = Instant.now();
        }
    }
}

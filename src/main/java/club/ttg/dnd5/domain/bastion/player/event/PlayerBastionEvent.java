package club.ttg.dnd5.domain.bastion.player.event;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.util.UUID;

/**
 * Событие бастиона: бросок 1к100 по таблице «События бастиона» за персонажа, который
 * обслуживает бастион, и его последствия. Событие, где нужен выбор игрока (взятка,
 * удачный случай, просьба о помощи), ждёт решения.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "player_bastion_event",
        indexes = @Index(name = "player_bastion_event_bastion_id_index", columnList = "bastion_id"))
public class PlayerBastionEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bastion_id", nullable = false)
    private UUID bastionId;

    /** Персонаж, за которого бросали; его игрок принимает решения события. */
    @Column(name = "member_id")
    private UUID memberId;

    /** Имя персонажа на момент события. */
    @Column(name = "character_name", length = 100)
    private String characterName;

    /** Ход, в который бастион обслуживали. */
    @Column(nullable = false)
    private int turn;

    /** Бросок 1к100. */
    @Column(nullable = false)
    private int roll;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BastionEventType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status = Status.RESOLVED;

    /** Что произошло — для журнала. */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private BastionEventDetails details = new BastionEventDetails();

    /** Принятое решение. */
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Decision decision;

    /** Какой предмет получил бастион — записывают игрок или Мастер. */
    @Column(columnDefinition = "TEXT")
    private String note;

    /** Событие, из-за которого бросили ещё раз (удачный случай). */
    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public boolean isPending() {
        return status == Status.PENDING;
    }

    /** Разрешено ли событие. */
    public enum Status {
        /** Ждёт решения игрока. */
        PENDING,
        /** Разрешено. */
        RESOLVED
    }

    /** Решение по событию. */
    public enum Decision {
        /** Заплатить: взятка за наёмника, 500 зм за удачный случай. */
        PAY,
        /** Отказаться. */
        REFUSE,
        /** Отправить защитников на помощь. */
        SEND,
        /** Записать полученный предмет. */
        RECORD
    }
}

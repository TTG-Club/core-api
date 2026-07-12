package club.ttg.dnd5.domain.tool.tracker.model;

import club.ttg.dnd5.domain.common.model.Timestamped;
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
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Трекер инициативы (инструмент): энкаунтер с участниками, порядком хода и раундами.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "initiative_tracker",
        indexes = {
                @Index(name = "initiative_tracker_owner_username_index", columnList = "owner_username")
        })
public class InitiativeTracker extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    /**
     * Логин владельца. NULL — анонимный трекер: владельца нет, доступ только по ключу
     * {@link #accessKey}; такие трекеры удаляются по TTL без активности.
     */
    @Column(name = "owner_username")
    private String ownerUsername;

    /**
     * Секретный ключ доступа. Возвращается клиенту при создании; аноним хранит его в localStorage
     * и передаёт в заголовке X-Tracker-Key. Для трекера с владельцем не используется —
     * доступ проверяется по логину из JWT.
     */
    @Column(name = "access_key", nullable = false)
    private UUID accessKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrackerStatus status;

    /**
     * Номер раунда боя: 0 — бой не начат, с 1 — идёт бой.
     */
    @Column(nullable = false)
    private int round;

    /**
     * Опция «новая инициатива каждый раунд»: если true — при переходе на новый раунд всем живым
     * участникам инициатива перебрасывается заново, и порядок хода пересобирается.
     */
    @Column(name = "reroll_each_round", nullable = false)
    private boolean rerollEachRound;

    /**
     * id участника, чей сейчас ход. NULL — бой не начат (или текущий участник был единственным и удалён).
     */
    @Column(name = "current_participant_id")
    private UUID currentParticipantId;

    /**
     * Мягкое удаление: трекер скрыт из списка, но остаётся в истории создания владельца.
     * Анонимные трекеры удаляются физически (истории у анонима нет).
     */
    @Column(nullable = false)
    private boolean deleted;
}

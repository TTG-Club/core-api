package club.ttg.dnd5.domain.bastion.player.model;

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

import java.time.Instant;
import java.util.UUID;

/**
 * Право другого пользователя править бастион — от запроса до решения владельца.
 *
 * <p>Просить может только тот, кто открыл бастион по ссылке «поделиться»: перебирать
 * пользователей некому. Записи бастиона удаляет каскад базы, а при отзыве ссылки их убирает
 * сервис.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "player_bastion_editor",
        indexes = {
                @Index(name = "player_bastion_editor_bastion_user_index",
                        columnList = "bastion_id, user_id", unique = true),
                @Index(name = "player_bastion_editor_user_id_index", columnList = "user_id")
        })
public class PlayerBastionEditor extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bastion_id", nullable = false)
    private UUID bastionId;

    /** Кто просит право; в паре с бастионом уникален — повторный запрос не плодит записи. */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status;

    /** Когда владелец разрешил; {@code null} — запрос ещё ждёт ответа. */
    @Column(name = "decided_at")
    private Instant decidedAt;

    /** Запрос ждёт владельца или право выдано. Отклонённый запрос удаляется. */
    public enum Status { PENDING, APPROVED }
}

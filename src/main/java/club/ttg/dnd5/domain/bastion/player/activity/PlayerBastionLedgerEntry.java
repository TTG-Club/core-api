package club.ttg.dnd5.domain.bastion.player.activity;

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

import java.time.Instant;
import java.util.UUID;

/**
 * Движение казны бастиона. Строки не меняются и не удаляются: казна — это сумма журнала,
 * а в самом бастионе хранится только итог для быстрого чтения.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "player_bastion_ledger",
        indexes = @Index(name = "player_bastion_ledger_bastion_id_index", columnList = "bastion_id"))
public class PlayerBastionLedgerEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bastion_id", nullable = false)
    private UUID bastionId;

    /** Ход, в который прошло движение. */
    @Column(nullable = false)
    private int turn;

    /** Сумма: плюс — поступление, минус — трата, зм. */
    @Column(name = "amount_gp", nullable = false)
    private long amountGp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Reason reason;

    @Column(length = 500)
    private String note;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    /** Причина движения. */
    public enum Reason {
        /** Мастер пополнил казну. */
        DEPOSIT,
        /** Мастер списал из казны. */
        WITHDRAWAL,
        /** Цена приказа. */
        ORDER,
        /** Возврат цены отменённого приказа. */
        REFUND,
        /** Постройка базового сооружения. */
        BUILD,
        /** Расширение сооружения. */
        ENLARGE
    }
}

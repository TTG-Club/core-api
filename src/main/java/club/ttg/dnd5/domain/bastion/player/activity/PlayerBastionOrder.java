package club.ttg.dnd5.domain.bastion.player.activity;

import club.ttg.dnd5.domain.bastion.model.BastionOrder;
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
 * Приказ бастиона. Сооружению — «Изготовить», «Собирать» и т.п.; всему бастиону —
 * «Обслуживать» ({@link #facilityId} пуст).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "player_bastion_order",
        indexes = @Index(name = "player_bastion_order_bastion_id_index", columnList = "bastion_id"))
public class PlayerBastionOrder extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bastion_id", nullable = false)
    private UUID bastionId;

    /** Сооружение персонажа; {@code null} — приказ всему бастиону. */
    @Column(name = "facility_id")
    private UUID facilityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_code", nullable = false, length = 16)
    private BastionOrder order;

    /** Вариант приказа из справочника: «книгу», «лейтенанта». */
    @Column(name = "option_name", length = 200)
    private String optionName;

    /** Сколько списано из казны при отдаче приказа, зм. */
    @Column(name = "cost_gp", nullable = false)
    private long costGp;

    @Column(name = "given_on_turn", nullable = false)
    private int givenOnTurn;

    @Column(name = "completes_on_turn", nullable = false)
    private int completesOnTurn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OrderStatus status = OrderStatus.ACTIVE;

    /** Кто отдал приказ. */
    @Column(name = "given_by", nullable = false)
    private UUID givenBy;

    /** Итог приказа: что получили, что узнали. Вписывает мастер при ходе. */
    @Column(columnDefinition = "TEXT")
    private String result;

    /** Пояснение игрока к приказу: тема исследования, что изготовить. */
    @Column(columnDefinition = "TEXT")
    private String note;

    public enum OrderStatus { ACTIVE, COMPLETED, CANCELLED }
}

package club.ttg.dnd5.domain.bastion.player.model;

import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сооружение персонажа в бастионе — экземпляр сооружения из справочника
 * ({@code bastion_facility}).
 *
 * <p>Принадлежит персонажу, а не бастиону целиком: лимиты специализированных сооружений
 * и стартовые базовые считаются на персонажа.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "player_bastion_facility")
public class PlayerBastionFacility extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private PlayerBastionMember member;

    /** Сооружение справочника. */
    @Column(name = "facility_url", nullable = false)
    private String facilityUrl;

    /**
     * Пространство экземпляра. У базового сооружения его выбирает игрок, у
     * специализированного оно берётся из справочника.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FacilitySpace space;

    /**
     * Требование сооружения (фокусировка и т.п.) подтверждено мастером. У сооружений без
     * требования — всегда {@code true}.
     */
    @Column(name = "prerequisite_confirmed", nullable = false)
    private boolean prerequisiteConfirmed;

    /** Сделанные выборы: тип сада, мастер тренировочной зоны и т.п. */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<SelectedChoice> choices = new ArrayList<>();

    /** Готово или строится. Стартовые сооружения готовы сразу. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FacilityStatus status = FacilityStatus.READY;

    /** Ход, на котором стройка закончится; у готовых — {@code null}. */
    @Column(name = "ready_on_turn")
    private Integer readyOnTurn;

    /**
     * Пространство, до которого сооружение расширяется. Пока идёт расширение, сооружение
     * работает в прежнем пространстве; {@code null} — не расширяется.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "pending_space", length = 16)
    private FacilitySpace pendingSpace;

    /** Ход, на котором закончится расширение. */
    @Column(name = "pending_ready_on_turn")
    private Integer pendingReadyOnTurn;

    public boolean isReady() {
        return status == FacilityStatus.READY;
    }
}

package club.ttg.dnd5.domain.bastion.player.model;

import club.ttg.dnd5.domain.common.model.Timestamped;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Игрок с доступом к бастиону и его персонаж.
 *
 * <p>Лимиты специализированных сооружений считаются на персонажа, поэтому у каждого
 * участника свой уровень. Уровень вводит мастер; привязка к листу персонажа
 * ({@link #characterSheetId}) необязательна и уровень не переопределяет.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "player_bastion_member",
        uniqueConstraints = @UniqueConstraint(name = "player_bastion_member_user_unique",
                columnNames = {"bastion_id", "user_id"}))
public class PlayerBastionMember extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bastion_id", nullable = false)
    private PlayerBastion bastion;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "character_name", nullable = false, length = 100)
    private String characterName;

    @Column(name = "character_level", nullable = false)
    private int characterLevel;

    @Column(name = "character_sheet_id")
    private UUID characterSheetId;

    /**
     * Сооружения персонажа. Удаление участника из бастиона удаляет и их — каскадом
     * Hibernate, до удаления самого участника.
     */
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @BatchSize(size = 50)
    private List<PlayerBastionFacility> facilities = new ArrayList<>();
}

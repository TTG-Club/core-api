package club.ttg.dnd5.domain.bastion.player.model;

import club.ttg.dnd5.domain.common.model.Timestamped;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Бастион игроков в игре из каталога (find-game-api).
 *
 * <p>Игра живёт в соседнем сервисе, здесь от неё только {@link #gameId} и мастер на момент
 * создания. Права каждый раз проверяются по актуальному составу игры, а {@link #masterId}
 * нужен, чтобы находить бастионы мастера без похода в каталог.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "player_bastion",
        indexes = @Index(name = "player_bastion_game_id_index", columnList = "game_id"))
public class PlayerBastion extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "game_id", nullable = false)
    private UUID gameId;

    @Column(name = "master_id", nullable = false)
    private UUID masterId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PlayerBastionStatus status = PlayerBastionStatus.SETUP;

    /** Номер текущего хода бастиона; ход — 7 игровых дней. */
    @Column(nullable = false)
    private int turn;

    /** Казна бастиона, зм. Пополняет и списывает только мастер. */
    @Column(name = "treasury_gp", nullable = false)
    private long treasuryGp;

    /** Оптимистическая блокировка: мастер и игроки правят бастион одновременно. */
    @Version
    @Column(nullable = false)
    private long version;

    @OneToMany(mappedBy = "bastion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<PlayerBastionMember> members = new ArrayList<>();
}

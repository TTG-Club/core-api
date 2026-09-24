package club.ttg.dnd5.domain.bastion.player.plan;

import club.ttg.dnd5.domain.common.model.Timestamped;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.UUID;

/**
 * План бастиона. Своей записью со своей версией, а не полем бастиона: план рисуют
 * одновременно несколько игроков, и общая с бастионом версия превращала бы каждый
 * мазок кистью в конфликт с выбором сооружений и правкой состава.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "player_bastion_plan")
public class PlayerBastionPlan extends Timestamped {
    @Id
    @Column(name = "bastion_id")
    private UUID bastionId;

    /** {@code null} — плана ещё нет в базе: Spring Data сохранит его вставкой. */
    @Version
    private Long version;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private PlanDocument document;
}

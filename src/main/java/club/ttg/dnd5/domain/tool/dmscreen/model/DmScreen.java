package club.ttg.dnd5.domain.tool.dmscreen.model;

import club.ttg.dnd5.domain.common.model.Timestamped;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Ширма Мастера (инструмент): окна пользователя одним jsonb-массивом. У пользователя ширма одна —
 * без имени и без списка, поэтому владелец уникален.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "dm_screen",
        indexes = @Index(name = "dm_screen_user_id_index", columnList = "user_id", unique = true))
public class DmScreen extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Владелец — uuid пользователя из JWT (subject). Ширму видит только он.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Окна в порядке, в котором их прислал клиент. Сохраняются целиком при каждой записи.
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<DmScreenWindow> windows = new ArrayList<>();
}

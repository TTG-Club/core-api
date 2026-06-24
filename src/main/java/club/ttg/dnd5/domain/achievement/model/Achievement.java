package club.ttg.dnd5.domain.achievement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Каталог достижений. Правится админом без деплоя. Достижение выдаётся либо
 * вручную, либо автоматически — по совпадению {@link #triggerKey} с событием
 * в коде (см. {@code AchievementService#grantByTrigger}).
 */
@Getter
@Setter
@Entity
@Table(name = "achievement")
public class Achievement {
    /** Стабильный машинный код, например {@code FIRST_CHARACTER}. Привязывается к кодам погашения. */
    @Id
    @Column(name = "code", length = 64)
    private String code;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 1024)
    private String description;

    /** Ссылка/идентификатор иконки достижения. */
    @Column(name = "icon")
    private String icon;

    /** Скрытое достижение: не показывать в общем списке, пока не получено. */
    @Column(name = "hidden", nullable = false)
    private boolean hidden;

    /**
     * Ключ автоматической выдачи. Если задан — достижение выдаётся при наступлении
     * соответствующего события. null — выдаётся только вручную или через код.
     */
    @Column(name = "trigger_key", length = 64)
    private String triggerKey;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp(source = SourceType.DB)
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp(source = SourceType.DB)
    private Instant updatedAt;
}

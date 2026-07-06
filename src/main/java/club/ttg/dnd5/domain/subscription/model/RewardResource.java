package club.ttg.dnd5.domain.subscription.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Конфиг контента награды: ссылка, статус готовности и заметка. Правится без
 * деплоя — например, включить приключение для всех держателей перка разом.
 */
@Getter
@Setter
@Entity
@Table(name = "reward_resource")
public class RewardResource {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "perk", length = 48)
    private RewardPerk perk;

    @Column(name = "title")
    private String title;

    /** Ссылка на скачивание/контакт/ассет; может быть пустой, пока готовится. */
    @Column(name = "url")
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability", nullable = false, length = 32)
    private RewardResourceAvailability availability;

    @Column(name = "note")
    private String note;

    @Column(name = "updated_at")
    @UpdateTimestamp(source = SourceType.DB)
    private Instant updatedAt;
}

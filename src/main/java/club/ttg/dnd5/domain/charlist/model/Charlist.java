package club.ttg.dnd5.domain.charlist.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "charlists")
public class Charlist {
    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** UUID владельца чарлиста */
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    /** Имя персонажа */
    @Column(name = "character_name", nullable = false)
    private String characterName;

    /** Уровень персонажа */
    @Column(name = "character_level")
    private Integer characterLevel;

    /** Класс(ы) персонажа (при мультиклассировании через запятую или JSON) */
    @Column(name = "character_class")
    private String characterClass;

    /** JSON-данные чарлиста, присылаемые фронтом */
    @Column(name = "data", columnDefinition = "TEXT")
    private String data;

    /** Уровень доступа */
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private CharlistVisibility visibility = CharlistVisibility.PRIVATE;

    /** Токен для доступа по ссылке */
    @Column(name = "share_token", unique = true)
    private String shareToken;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp(source = SourceType.DB)
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp(source = SourceType.DB)
    private Instant updatedAt;
}
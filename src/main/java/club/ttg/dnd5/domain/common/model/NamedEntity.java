package club.ttg.dnd5.domain.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

@Getter
@Setter
@MappedSuperclass
public abstract class NamedEntity extends Timestamped implements Persistable<String> {
    @Id
    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String english;
    private String alternative;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String imageUrl;
    /**
     * Indicates whether this entity should be hidden from the frontend.
     * <p>
     * If {@code true}, this entity is considered outdated or irrelevant, and it will not be included
     * in responses sent to the frontend. If {@code false}, the entity will be visible to the frontend.
     * </p>
     */
    @Column(name = "is_hidden_entity")
    private boolean isHiddenEntity = false;

    /** Версия SRD, например "5.1" */
    @Column(name = "srd_version")
    private String srdVersion;

    @Override
    @Transient
    public String getId() {
        return url;
    }

    /**
     * Определяет, является ли сущность новой (ещё не сохранённой в БД).
     * Используется Spring Data JPA для выбора между persist() и merge().
     * createdAt устанавливается БД при INSERT, поэтому null означает новую сущность.
     */
    @Override
    @Transient
    public boolean isNew() {
        return getCreatedAt() == null;
    }
}

package club.ttg.dnd5.domain.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class NamedEntity extends Timestamped {
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
}

package club.ttg.dnd5.model.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@MappedSuperclass
public abstract class NamedEntity extends TimestampedEntity {
    @Id
    @Column(nullable = false, unique = true)
    private String url;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String english;
    private String alternative;
    private String shortName;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String imageUrl;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> galleryUrl = new ArrayList<>();
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

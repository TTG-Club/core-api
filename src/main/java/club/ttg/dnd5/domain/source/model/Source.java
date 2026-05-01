package club.ttg.dnd5.domain.source.model;

import club.ttg.dnd5.domain.common.model.Timestamped;
import club.ttg.dnd5.domain.source.rest.dto.PublisherDto;
import club.ttg.dnd5.domain.source.rest.dto.TranslationDto;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "source")
public class Source extends Timestamped {
    @Id
    @Column(unique = true, nullable = false)
    private String acronym;

    @Column(unique = true, nullable = false)
    private String url;

    private String name;
    private String english;
    private String alternative;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Deprecated(forRemoval = true)
    private SourceType type;

    @Enumerated(EnumType.STRING)
    private SourceOrigin origin;

    @Enumerated(EnumType.STRING)
    private SourceKind kind;
    /**
     * Издатель
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private PublisherDto publisher;

    /**
     * Перевод
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private TranslationDto translation;

    private String image;

    /**
     * Список авторов, разделенных запятой
     */
    private String authors;

    public SourceOrigin getOrigin() {
        if (origin != null) {
            return origin;
        }
        return type == null ? null : type.toOrigin();
    }

    public SourceKind getKind() {
        if (kind != null) {
            return kind;
        }
        return type == null ? null : type.toKind();
    }

    @PrePersist
    @PreUpdate
    public void normalizeSourceClassification() {
        if (origin == null && type != null) {
            origin = type.toOrigin();
        }
        if (kind == null && type != null) {
            kind = type.toKind();
        }
        if (type == null && origin != null && kind != null) {
            type = SourceType.from(origin, kind);
        }
    }
}

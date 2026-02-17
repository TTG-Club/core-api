package club.ttg.dnd5.domain.source.model;

import club.ttg.dnd5.domain.common.model.Timestamped;
import club.ttg.dnd5.domain.source.rest.dto.PublisherDto;
import club.ttg.dnd5.domain.source.rest.dto.TranslationDto;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDate;

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
    private SourceType type;
    /**
     * Издатель
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private PublisherDto publisher;

    /**
     * Дата публикации
     */
    private LocalDate published;
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
}

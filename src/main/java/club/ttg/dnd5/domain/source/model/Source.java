package club.ttg.dnd5.domain.source.model;

import club.ttg.dnd5.domain.common.model.Timestamped;
import jakarta.persistence.*;
import lombok.*;

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
    private LocalDate published;
    private String image;

    // список авторов, разделенных запятой
    private String authors;
}

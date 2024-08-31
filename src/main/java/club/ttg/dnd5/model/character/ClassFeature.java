package club.ttg.dnd5.model.character;

import club.ttg.dnd5.model.Source;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "class_features",
        indexes = {@Index(name = "url_index", columnList = "url")}
)
public class ClassFeature {
    @Id
    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String english;
    private String alternative;
    @Column(columnDefinition = "TEXT")
    private String original;

    private short level;
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "source")
    private Source source;
    private Short page;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime created;
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime lastUpdated;
}

package club.ttg.dnd5.model.character;

import club.ttg.dnd5.dictionary.Ability;
import club.ttg.dnd5.model.Source;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "backgrounds",
        indexes = {@Index(name = "url_index", columnList = "url")}
)
public class Background {
    @Id
    private String url;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String english;
    private String alternative;

    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(columnDefinition = "TEXT")
    private String original;

    @Enumerated(EnumType.STRING)
    private Set<Ability> abilities;

    @ManyToOne
    @JoinColumn(name = "source")
    private Source source;
    private Short page;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime created;
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime lastUpdated;
}

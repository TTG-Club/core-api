package club.ttg.dnd5.model.bestiary;

import club.ttg.dnd5.dictionary.Alignment;
import club.ttg.dnd5.dictionary.Size;
import club.ttg.dnd5.dictionary.beastiary.CreatureType;
import club.ttg.dnd5.model.Source;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "bestiary")
public class Beast  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String english;
    private String alternative;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private Size size;
    @Enumerated(EnumType.STRING)
    private CreatureType type;
    private String tags;
    @Enumerated(EnumType.STRING)
    private Alignment alignment;

    private byte AC;
    private String descriptionAC;

    private short hit;
    private String descriptionHit;

    @ManyToOne
    @JoinColumn(name = "source")
    private Source source;
    private Short page;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime created;
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime lastUpdated;
}

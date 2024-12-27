package club.ttg.dnd5.model.species;

import club.ttg.dnd5.model.base.CreatureProperties;
import club.ttg.dnd5.model.base.HasSourceEntity;
import club.ttg.dnd5.model.base.Tag;
import club.ttg.dnd5.model.book.Source;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

import java.util.*;

/**
 Виды или разновидности (расы)
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "species",
        indexes = {@Index(name = "url_index", columnList = "url")}
)
public class Species extends CreatureProperties implements HasSourceEntity {
    private String linkImageUrl; //для бэкграунда

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Species parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Species> subSpecies = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "source")
    private Source source = new Source();

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "species_tags",
            joinColumns = @JoinColumn(name = "species_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "species_url")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Collection<SpeciesFeature> features;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "species_gallery", joinColumns = @JoinColumn(name = "species_id"))
    @Column(name = "gallery_url")
    private List<String> galleryUrl = new ArrayList<>();
}

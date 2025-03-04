package club.ttg.dnd5.domain.species.model;

import club.ttg.dnd5.domain.book.model.Book;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 Виды или разновидности (расы)
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "species",
        indexes = {
                @Index(name = "url_index", columnList = "url"),
                @Index(name = "name_index", columnList = "name, english, alternative")
        }
)
public class Species extends CreatureProperties {

    /** Умения */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "species_url")
    private Collection<SpeciesFeature> features;

    private String linkImageUrl; //для изоброжения бэкграунда

    /** Родительский вид */
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Species parent;

    /** Происхождения */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, orphanRemoval = true)
    private Collection<Species> lineages;

    /** источник */
    @ManyToOne
    @JoinColumn(name = "source")
    private Book source;

    /** Ссылки на изображения для галереи */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "species_gallery", joinColumns = @JoinColumn(name = "species_id"))
    @Column(name = "gallery_url")
    private List<String> galleryUrl;

}

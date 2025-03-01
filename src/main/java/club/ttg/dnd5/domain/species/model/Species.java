package club.ttg.dnd5.domain.species.model;

import club.ttg.dnd5.domain.book.model.Source;
import club.ttg.dnd5.domain.common.model.HasSourceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

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
public class Species extends CreatureProperties implements HasSourceEntity {
    private String linkImageUrl; //для изоброжения бэкграунда

    /** Родительский вид */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Species parent;

    /** Происхождения */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Species> lineages = new ArrayList<>();

    /** источник */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "source")
    private Source source = new Source();


    /** Умения */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "species_url")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Collection<SpeciesFeature> features;

    /** Ссылки на изображения для галереи */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "species_gallery", joinColumns = @JoinColumn(name = "species_id"))
    @Column(name = "gallery_url")
    private List<String> galleryUrl = new ArrayList<>();
}

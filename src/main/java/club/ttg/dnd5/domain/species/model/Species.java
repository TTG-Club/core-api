package club.ttg.dnd5.domain.species.model;

import club.ttg.dnd5.domain.book.model.Book;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

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
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<SpeciesFeature> features;

    private String linkImageUrl; //для изоброжения бэкграунда

    /** Родительский вид */
    @ManyToOne
    @JoinColumn(name = "parent_url")
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

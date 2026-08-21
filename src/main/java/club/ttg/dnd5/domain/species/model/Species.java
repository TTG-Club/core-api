package club.ttg.dnd5.domain.species.model;

import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.species.model.mechanics.SpeciesMechanics;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.Collection;

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

    /**
     * Механика влияния самого вида или происхождения на лист персонажа: то, что даёт
     * выбор записи целиком, а не отдельное её умение.
     *
     * <p>Нужна прежде всего происхождениям: умений у них нет — правило целиком лежит в
     * описании, — и приписать сопротивление инфернального тифлинга или скорость лесного
     * эльфа было бы больше некуда. Виду с умениями это поле обычно не нужно: там эффект
     * лучше держать у того умения, которое его даёт.</p>
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private SpeciesMechanics mechanics;

    private String linkImageUrl; //для изоброжения бэкграунда

    /** Родительский вид */
    @ManyToOne
    @JoinColumn(name = "parent_url")
    private Species parent;

    /** Происхождения */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private Collection<Species> lineages;

    /** источник */
    @ManyToOne
    @JoinColumn(name = "source")
    private Source source;
    private Long sourcePage;

    /** Ссылки на изображения для галереи */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "species_gallery", joinColumns = @JoinColumn(name = "species_id"))
    @Column(name = "gallery_url")
    private Collection<String> galleryUrl;

}

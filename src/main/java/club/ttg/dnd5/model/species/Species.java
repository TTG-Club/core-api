package club.ttg.dnd5.model.species;

import club.ttg.dnd5.model.Source;
import club.ttg.dnd5.model.base.CreatureProperties;
import club.ttg.dnd5.model.base.HasSourceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;

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
    private Short page;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Species parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Species> subSpecies = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "source")
    private Source source;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "species_url")
    private Collection<SpeciesFeature> features;

    public void setSource(String sourceName) {
        if (this.source == null) {
            source = new Source();
        }
        this.source.setPage(this.page);
        this.source.setSource(sourceName);
    }

    @Override
    public void setSource(Source source) {
        this.source = source;
    }
}

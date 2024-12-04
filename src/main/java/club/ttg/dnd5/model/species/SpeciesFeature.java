package club.ttg.dnd5.model.species;

import club.ttg.dnd5.model.base.FeatureBase;
import club.ttg.dnd5.model.base.HasSourceEntity;
import club.ttg.dnd5.model.base.HasTagEntity;
import club.ttg.dnd5.model.base.Tag;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "species_features",
        indexes = {@Index(name = "url_index", columnList = "url")}
)
@Getter
@Setter
public class SpeciesFeature extends FeatureBase implements HasTagEntity, HasSourceEntity {

    @ManyToMany
    @JoinTable(
            name = "feature_tags",  // Name of the junction table
            joinColumns = @JoinColumn(name = "feature_id"),  // Foreign key for SpeciesFeature (FeatureBase)
            inverseJoinColumns = @JoinColumn(name = "tag_id")  // Foreign key for Tag
    )
    private Set<Tag> tags = new HashSet<>();
}

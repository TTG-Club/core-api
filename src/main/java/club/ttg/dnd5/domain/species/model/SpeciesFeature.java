package club.ttg.dnd5.domain.species.model;

import club.ttg.dnd5.domain.common.model.FeatureBase;
import club.ttg.dnd5.domain.common.model.HasSourceEntity;
import club.ttg.dnd5.domain.common.model.HasTagEntity;
import club.ttg.dnd5.domain.common.model.Tag;
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "feature_tags",  // Name of the junction table
            joinColumns = @JoinColumn(name = "feature_id"),  // Foreign key for SpeciesFeature (FeatureBase)
            inverseJoinColumns = @JoinColumn(name = "tag_id")  // Foreign key for Tag
    )
    private Set<Tag> tags = new HashSet<>();
}

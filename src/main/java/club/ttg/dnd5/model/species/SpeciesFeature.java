package club.ttg.dnd5.model.species;

import club.ttg.dnd5.model.base.FeatureBase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "species_features",
        indexes = {@Index(name = "url_index", columnList = "url")}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpeciesFeature extends FeatureBase {
    @ElementCollection
    @CollectionTable(name = "species_feature_entries", joinColumns = @JoinColumn(name = "species_feature_id"))
    @Column(name = "entry")
    private List<String> entries;
}

package club.ttg.dnd5.model.species;

import club.ttg.dnd5.model.base.FeatureBase;
import club.ttg.dnd5.model.base.HasSourceEntity;
import club.ttg.dnd5.model.base.HasTags;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "species_features",
        indexes = {@Index(name = "url_index", columnList = "url")}
)
@Getter
@Setter
public class SpeciesFeature extends FeatureBase implements HasTags, HasSourceEntity {
}

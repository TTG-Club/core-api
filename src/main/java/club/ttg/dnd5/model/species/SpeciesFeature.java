package club.ttg.dnd5.model.species;

import club.ttg.dnd5.model.base.FeatureBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "species_features",
        indexes = {@Index(name = "url_index", columnList = "url")}
)
public class SpeciesFeature extends FeatureBase {

}

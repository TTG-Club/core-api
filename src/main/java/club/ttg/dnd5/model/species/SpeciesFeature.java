package club.ttg.dnd5.model.species;

import club.ttg.dnd5.model.base.FeatureBase;
import club.ttg.dnd5.model.base.HasSourceEntity;
import club.ttg.dnd5.model.base.HasTags;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "species_features",
        indexes = {@Index(name = "url_index", columnList = "url")}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpeciesFeature extends FeatureBase implements HasTags, HasSourceEntity {
    @ElementCollection
    @CollectionTable(name = "species_feature_entries", joinColumns = @JoinColumn(name = "species_feature_id"))
    @Column(name = "entry")
    private List<String> entries;
    // Храним карту tags как отдельную таблицу с ключами и значениями
    @ElementCollection
    @CollectionTable(name = "entity_tags", joinColumns = @JoinColumn(name = "entity_url"))
    @MapKeyColumn(name = "tag_key")
    @Column(name = "tag_value")
    private Map<String, String> tags = new HashMap<>();
}

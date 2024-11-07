package club.ttg.dnd5.model.base;

import club.ttg.dnd5.model.book.Source;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@MappedSuperclass
public abstract class FeatureBase extends NamedEntity {
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "source")
    private Source source;
    private String featureDescription;
    // Храним карту tags как отдельную таблицу с ключами и значениями
    @ElementCollection
    @CollectionTable(name = "entity_tags", joinColumns = @JoinColumn(name = "entity_url"))
    @MapKeyColumn(name = "tag_key")
    @Column(name = "tag_value")
    private Map<String, String> tags = new HashMap<>();
}

package club.ttg.dnd5.model.character;

import club.ttg.dnd5.model.base.FeatureBase;
import club.ttg.dnd5.model.base.HasSourceEntity;
import club.ttg.dnd5.model.base.HasTagEntity;
import club.ttg.dnd5.model.base.Tag;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "class_features",
        indexes = {@Index(name = "url_index", columnList = "url")}
)
public class ClassFeature extends FeatureBase implements HasSourceEntity, HasTagEntity {
    private short level;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "class_feature_tags", // Name of the join table
            joinColumns = @JoinColumn(name = "class_feature_id"), // Foreign key for ClassFeature
            inverseJoinColumns = @JoinColumn(name = "tag_id") // Foreign key for Tag
    )
    private Set<Tag> tags = new HashSet<>();
}

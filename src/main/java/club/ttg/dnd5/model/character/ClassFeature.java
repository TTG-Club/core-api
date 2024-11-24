package club.ttg.dnd5.model.character;

import club.ttg.dnd5.model.base.FeatureBase;
import club.ttg.dnd5.model.base.HasSourceEntity;
import club.ttg.dnd5.model.base.HasTags;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "class_features",
        indexes = {@Index(name = "url_index", columnList = "url")}
)
public class ClassFeature extends FeatureBase implements HasTags, HasSourceEntity {
    private short level;
}

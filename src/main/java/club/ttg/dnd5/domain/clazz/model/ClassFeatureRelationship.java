package club.ttg.dnd5.domain.clazz.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "class_feature_relationships")
@Getter
@Setter
public class ClassFeatureRelationship {

    @EmbeddedId
    private ClassFeatureRelationshipId id;

    private Integer level;
    private Boolean isHiddenEntity;

}

package club.ttg.dnd5.domain.clazz.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ClassFeatureRelationshipId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "class_url", referencedColumnName = "url")
    private ClassCharacter classUrl;

    @ManyToOne
    @JoinColumn(name = "feature_url", referencedColumnName = "url")
    private ClassFeature featureUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassFeatureRelationshipId that = (ClassFeatureRelationshipId) o;
        return Objects.equals(classUrl, that.classUrl) && Objects.equals(featureUrl, that.featureUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classUrl, featureUrl);
    }
}

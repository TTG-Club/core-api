package club.ttg.dnd5.model.base;

import club.ttg.dnd5.model.book.Source;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class FeatureBase extends NamedEntity {
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "source")
    private Source source;
    private String featureDescription;
}

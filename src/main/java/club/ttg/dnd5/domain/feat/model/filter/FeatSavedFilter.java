package club.ttg.dnd5.domain.feat.model.filter;

import club.ttg.dnd5.domain.filter.model.AbstractSavedFilter;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = "FEAT_FILTER")
public class FeatSavedFilter extends AbstractSavedFilter {
}

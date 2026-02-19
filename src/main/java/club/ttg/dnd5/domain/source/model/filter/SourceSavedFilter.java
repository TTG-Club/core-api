package club.ttg.dnd5.domain.source.model.filter;

import club.ttg.dnd5.domain.filter.model.AbstractSavedFilter;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = "SOURCE_FILTER")
public class SourceSavedFilter extends AbstractSavedFilter {
}

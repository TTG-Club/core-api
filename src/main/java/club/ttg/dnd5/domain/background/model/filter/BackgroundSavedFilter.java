package club.ttg.dnd5.domain.background.model.filter;

import club.ttg.dnd5.domain.filter.model.AbstractSavedFilter;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = "BACKGROUND_FILTER")
public class BackgroundSavedFilter extends AbstractSavedFilter {
}

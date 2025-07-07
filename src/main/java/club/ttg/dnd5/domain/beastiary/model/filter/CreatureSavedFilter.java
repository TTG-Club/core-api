package club.ttg.dnd5.domain.beastiary.model.filter;

import club.ttg.dnd5.domain.filter.model.AbstractSavedFilter;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = "CREATURE_FILTER")
public class CreatureSavedFilter extends AbstractSavedFilter {
}

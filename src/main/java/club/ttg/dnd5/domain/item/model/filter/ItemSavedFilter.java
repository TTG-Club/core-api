package club.ttg.dnd5.domain.item.model.filter;

import club.ttg.dnd5.domain.filter.model.AbstractSavedFilter;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = "ITEM_FILTER")
public class ItemSavedFilter extends AbstractSavedFilter {
}

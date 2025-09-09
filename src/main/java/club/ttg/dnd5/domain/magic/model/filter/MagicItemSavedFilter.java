package club.ttg.dnd5.domain.magic.model.filter;

import club.ttg.dnd5.domain.filter.model.AbstractSavedFilter;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = "MAGIC_ITEM_FILTER")
public class MagicItemSavedFilter extends AbstractSavedFilter {
}

package club.ttg.dnd5.domain.spell.model.filter;

import club.ttg.dnd5.domain.filter.model.AbstractSavedFilter;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = "SPELL_FILTER")
public class SpellSavedFilter extends AbstractSavedFilter {
}

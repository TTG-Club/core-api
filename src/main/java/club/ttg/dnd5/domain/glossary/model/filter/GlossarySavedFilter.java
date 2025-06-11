package club.ttg.dnd5.domain.glossary.model.filter;

import club.ttg.dnd5.domain.filter.model.AbstractSavedFilter;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = "GLOSSARY_FILTER")
public class GlossarySavedFilter extends AbstractSavedFilter {
}

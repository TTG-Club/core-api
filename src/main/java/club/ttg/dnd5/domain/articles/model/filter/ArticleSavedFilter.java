package club.ttg.dnd5.domain.articles.model.filter;

import club.ttg.dnd5.domain.filter.model.AbstractSavedFilter;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(value = "Article_FILTER")
public class ArticleSavedFilter extends AbstractSavedFilter {
}

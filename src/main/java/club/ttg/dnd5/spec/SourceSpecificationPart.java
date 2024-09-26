package club.ttg.dnd5.spec;

import club.ttg.dnd5.dto.engine.FilterDto;
import club.ttg.dnd5.dto.engine.SearchRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public abstract class SourceSpecificationPart<T> implements SpecificationPart<T> {
    @Override
    public Specification<T> toSpecification(SearchRequest request) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (request.getFilters() != null) {
                FilterDto filter = request.getFilters().get("source");
                if (filter != null && filter.getValues() != null && !filter.getValues().isEmpty()) {
                    predicate = cb.and(predicate, root.get("source").in(filter.getValues()));
                }
            }
            return predicate;
        };
    }
}

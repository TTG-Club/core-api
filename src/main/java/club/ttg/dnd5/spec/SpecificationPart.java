package club.ttg.dnd5.spec;

import club.ttg.dnd5.dto.engine.SearchRequest;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationPart<T> {
    Specification<T> toSpecification(SearchRequest request);
}

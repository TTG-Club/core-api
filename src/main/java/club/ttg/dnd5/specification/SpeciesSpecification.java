package club.ttg.dnd5.specification;

import club.ttg.dnd5.dto.engine.SearchRequest;
import club.ttg.dnd5.model.species.Species;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SpeciesSpecification implements SpecificationPart<Species> {
    private final List<SpecificationPart<Species>> specificationParts = new ArrayList<>();

    public SpeciesSpecification() {
        specificationParts.add(new NameSpecificationPart<>() {});
        specificationParts.add(new SourceSpecificationPart<>() {});
        // Можно добавить другие спецификаци
    }

    @Override
    public Specification<Species> toSpecification(SearchRequest request) {
        Specification<Species> result = Specification.where(null);
        for (SpecificationPart<Species> part : specificationParts) {
            result = result.and(part.toSpecification(request));
        }
        return result;
    }
}

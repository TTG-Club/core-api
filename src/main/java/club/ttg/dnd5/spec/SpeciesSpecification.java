package club.ttg.dnd5.spec;

import club.ttg.dnd5.dto.engine.SearchRequest;
import club.ttg.dnd5.model.species.Species;
import club.ttg.dnd5.spec.part.NameSpecificationPart;
import club.ttg.dnd5.spec.part.SourceSpecificationPart;
import club.ttg.dnd5.spec.part.SpecificationPart;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SpeciesSpecification {
    private final List<SpecificationPart<Species>> specificationParts = new ArrayList<>();

    public SpeciesSpecification() {
        specificationParts.add(new NameSpecificationPart<>() {});
        specificationParts.add(new SourceSpecificationPart<>() {});
    }

    public Specification<Species> getDefaultSpecification(SearchRequest request) {
        Specification<Species> result = Specification.where(null);
        for (SpecificationPart<Species> part : specificationParts) {
            result = result.and(part.toSpecification(request));
        }
        return result;
    }
}

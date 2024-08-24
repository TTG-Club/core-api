package club.ttg.dnd5.service.species;

import club.ttg.dnd5.dto.engine.FilterDto;
import club.ttg.dnd5.dto.engine.SearchDto;
import club.ttg.dnd5.dto.engine.SearchRequest;
import club.ttg.dnd5.model.character.Species;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;

public class SpeciesSpecification {

    public static Specification<Species> buildSpecification(SearchRequest request) {
        return (Root<Species> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.conjunction(); // Создаем пустое условие

            if (request.getSearch() != null) {
                SearchDto searchDto = request.getSearch();
                if (searchDto.getValue() != null && !searchDto.getValue().isEmpty()) {
                    predicate = cb.and(predicate, cb.like(cb.lower(root.get("name")), "%" + searchDto.getValue().toLowerCase() + "%"));
                }
            }

            if (request.getFilters() != null) {
                for (Map.Entry<String, FilterDto> entry : request.getFilters().entrySet()) {
                    String field = entry.getKey();
                    FilterDto filter = entry.getValue();

                    if ("source".equals(field)) {
                        if (filter.getValues() != null && !filter.getValues().isEmpty()) {
                            predicate = cb.and(predicate, root.get("source").in(filter.getValues()));
                        }
                    }
                }
            }

            return predicate;
        };
    }
}

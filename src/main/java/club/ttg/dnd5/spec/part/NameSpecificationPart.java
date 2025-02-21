package club.ttg.dnd5.spec.part;

import club.ttg.dnd5.dto.engine.SearchDto;
import club.ttg.dnd5.dto.engine.SearchRequest;
import org.springframework.data.jpa.domain.Specification;

public abstract class NameSpecificationPart<T> implements SpecificationPart<T> {
    @Override
    public Specification<T> toSpecification(SearchRequest request) {
        return (root, query, cb) -> {
            if (request.getSearch() != null) {
                SearchDto searchDto = request.getSearch();
                if (searchDto.getValue() != null && !searchDto.getValue().isEmpty()) {
                    return cb.like(cb.lower(root.get("name")), "%" + searchDto.getValue().toLowerCase() + "%");
                }
            }
            return cb.conjunction(); // Пустая спецификация, если нет фильтрации по имени
        };
    }
}

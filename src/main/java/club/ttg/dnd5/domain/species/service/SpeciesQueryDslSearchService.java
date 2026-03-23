package club.ttg.dnd5.domain.species.service;

import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import club.ttg.dnd5.domain.species.model.QSpecies;
import club.ttg.dnd5.domain.species.model.Species;
import com.querydsl.core.types.OrderSpecifier;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

@Service
public class SpeciesQueryDslSearchService extends AbstractQueryDslSearchService<Species, QSpecies> {
    private static final QSpecies SPECIES = QSpecies.species;
    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{SPECIES.name.asc()};

    public SpeciesQueryDslSearchService(EntityManager entityManager) {
        super (entityManager, SPECIES);
    }

    @Override
    protected OrderSpecifier<?>[] getOrder() {
        return ORDER;
    }
}

package club.ttg.dnd5.domain.species.service;

import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import club.ttg.dnd5.domain.species.model.QSpecies;
import club.ttg.dnd5.domain.species.model.Species;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpeciesQueryDslSearchService extends AbstractQueryDslSearchService<Species, QSpecies> {
    private static final QSpecies SPECIES = QSpecies.species;
    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{SPECIES.name.asc()};

    public SpeciesQueryDslSearchService(EntityManager entityManager) {
        super (entityManager, SPECIES);
    }

    @Override
    protected BooleanExpression buildSourcePredicate(final List<String> values) {
        PathBuilder<Object> magicItem = new PathBuilder<>(Object.class, SPECIES.getMetadata());
        return magicItem.getString("source").in(values);    }

    @Override
    protected OrderSpecifier<?>[] getOrder() {
        return ORDER;
    }
}

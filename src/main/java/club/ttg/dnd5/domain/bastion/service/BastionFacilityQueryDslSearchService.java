package club.ttg.dnd5.domain.bastion.service;

import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import club.ttg.dnd5.domain.bastion.model.QBastionFacility;
import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BastionFacilityQueryDslSearchService
        extends AbstractQueryDslSearchService<BastionFacility, QBastionFacility> {
    private static final QBastionFacility FACILITY = QBastionFacility.bastionFacility;
    /** Как в книге: базовые, затем специализированные по уровню и по алфавиту. */
    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{
            FACILITY.category.asc(), FACILITY.level.asc().nullsFirst(), FACILITY.name.asc()};

    public BastionFacilityQueryDslSearchService(EntityManager entityManager) {
        super(entityManager, FACILITY);
    }

    @Override
    protected BooleanExpression buildSourcePredicate(final List<String> values) {
        PathBuilder<Object> facility = new PathBuilder<>(Object.class, BastionFacilityPredicateBuilder.ALIAS);
        return facility.getString("source").in(values);
    }

    @Override
    protected OrderSpecifier<?>[] getOrder() {
        return ORDER;
    }
}

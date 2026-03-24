package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.model.QFeat;
import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeatQueryDslSearchService extends AbstractQueryDslSearchService<Feat, QFeat> {
    private static final QFeat FEAT = QFeat.feat;
    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{FEAT.category.asc(), FEAT.name.asc()};

    public FeatQueryDslSearchService(EntityManager entityManager) {
        super(entityManager, FEAT);
    }

    @Override
    protected BooleanExpression buildSourcePredicate(final List<String> values) {
        PathBuilder<Object> creature = new PathBuilder<>(Object.class, "feat");
        return creature.getString("source").in(values);
    }

    @Override
    protected OrderSpecifier<?>[] getOrder() {
        return ORDER;
    }


}

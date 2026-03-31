package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.background.model.QBackground;
import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BackgroundQueryDslSearchService extends AbstractQueryDslSearchService<Background, QBackground> {
    private static final QBackground BACKGROUND = QBackground.background;
    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{BACKGROUND.name.asc()};

    public BackgroundQueryDslSearchService(EntityManager entityManager) {
        super(entityManager, BACKGROUND);
    }

    @Override
    protected BooleanExpression buildSourcePredicate(final List<String> values) {
        PathBuilder<Object> background = new PathBuilder<>(Object.class, "background");
        return background.getString("source").in(values);
    }

    @Override
    protected OrderSpecifier<?>[] getOrder() {
        return ORDER;
    }

}

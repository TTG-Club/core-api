package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.background.model.QBackground;
import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import com.querydsl.core.types.OrderSpecifier;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

@Service
public class BackgroundQueryDslSearchService extends AbstractQueryDslSearchService<Background, QBackground> {
    private static final QBackground BACKGROUND = QBackground.background;
    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{BACKGROUND.name.asc()};

    public BackgroundQueryDslSearchService(BackgroundFilterService filterService, EntityManager entityManager) {
        super(filterService, entityManager, BACKGROUND);
    }

    @Override
    protected OrderSpecifier<?>[] getOrder() {
        return ORDER;
    }
}

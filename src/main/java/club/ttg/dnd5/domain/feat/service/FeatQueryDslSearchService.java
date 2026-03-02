package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.model.QFeat;
import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import com.querydsl.core.types.OrderSpecifier;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

@Service
public class FeatQueryDslSearchService extends AbstractQueryDslSearchService<Feat, QFeat> {
    private static final QFeat FEAT = QFeat.feat;
    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{FEAT.category.asc(), FEAT.name.asc()};

    public FeatQueryDslSearchService(FeatFilterService filterService, SourceSavedFilterService sourceSavedFilterService,
                                     EntityManager entityManager) {
        super(filterService, sourceSavedFilterService, entityManager, FEAT);
    }

    @Override
    protected OrderSpecifier<?>[] getOrder() {
        return ORDER;
    }
}

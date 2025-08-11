package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.model.QFeat;
import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import com.querydsl.core.types.OrderSpecifier;
import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class FeatQueryDslSearchService extends AbstractQueryDslSearchService<Feat, QFeat> {
    private static final QFeat FEAT = QFeat.feat;

    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{
            FEAT.category.asc(),
            FEAT.name.asc()};

    public FeatQueryDslSearchService(FeatFilterService featFilterService,
                                     EntityManager entityManager) {
        super(featFilterService, entityManager, FEAT);
    }

    @Override
    protected OrderSpecifier<?>[] getOrder(String sort) {
        if (StringUtils.isBlank(sort)) {
            return ORDER;
        }

        String[] parts = sort.split(",");
        String field = parts[0].trim().toLowerCase();
        boolean descending = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim());

        OrderSpecifier<?> order = switch (field) {
            case "category" -> descending
                    ? QFeat.feat.category.desc()
                    : QFeat.feat.category.asc();
            case "name" -> descending
                    ? QFeat.feat.name.desc()
                    : QFeat.feat.name.asc();
            case "english" -> descending
                    ? QFeat.feat.english.desc()
                    : QFeat.feat.english.asc();
            default ->
                // fallback на name
                    QFeat.feat.name.asc();
        };

        return new OrderSpecifier[]{order};
    }
}

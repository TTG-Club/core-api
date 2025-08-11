package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.background.model.QBackground;
import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import com.querydsl.core.types.OrderSpecifier;
import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class BackgroundQDslSearchService extends AbstractQueryDslSearchService<Background, QBackground> {
    private static final QBackground BACKGROUND = QBackground.background;
    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{BACKGROUND.name.asc()};


    public BackgroundQDslSearchService(final BackgroundFilterService backgroundFilterService,
                                       final EntityManager entityManager) {
        super(backgroundFilterService, entityManager, BACKGROUND);
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
            case "name" -> descending
                    ? QBackground.background.name.desc()
                    : QBackground.background.name.asc();
            case "english" -> descending
                    ? QBackground.background.english.desc()
                    : QBackground.background.english.asc();
            default ->
                // fallback на name
                    QBackground.background.name.asc();
        };

        return new OrderSpecifier[]{order};
    }
}

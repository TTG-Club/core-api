package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.model.QItem;
import com.querydsl.core.types.OrderSpecifier;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

@Service
public class ItemQueryDslSearchService extends AbstractQueryDslSearchService<Item, QItem> {
    private static final QItem ITEM = QItem.item;
    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{
            ITEM.name.asc()
    };

    public ItemQueryDslSearchService(ItemFilterService filterService, EntityManager entityManager) {
        super(filterService, entityManager, ITEM);
    }

    @Override
    protected OrderSpecifier<?>[] getDefaultOrder() {
        return ORDER;
    }
}

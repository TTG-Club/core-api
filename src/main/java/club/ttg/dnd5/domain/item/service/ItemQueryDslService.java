package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.model.QItem;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemQueryDslService extends AbstractQueryDslSearchService<Item, QItem> {
    private static final QItem ITEM = QItem.item;
    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{ITEM.name.asc()};

    public ItemQueryDslService(EntityManager entityManager) {
        super(entityManager, ITEM);
    }

    @Override
    protected BooleanExpression buildSourcePredicate(final List<String> values) {
        PathBuilder<Object> creature = new PathBuilder<>(Object.class, "item");
        return creature.getString("source").in(values);
    }

    @Override
    protected OrderSpecifier<?>[] getOrder() {
        return ORDER;
    }
}
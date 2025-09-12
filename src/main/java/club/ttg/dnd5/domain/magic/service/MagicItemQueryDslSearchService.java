package club.ttg.dnd5.domain.magic.service;

import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.model.QMagicItem;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

@Service
public class MagicItemQueryDslSearchService extends AbstractQueryDslSearchService<MagicItem, QMagicItem> {
    private static final QMagicItem MAGIC_ITEM = QMagicItem.magicItem;

    public MagicItemQueryDslSearchService(MagicItemFilterService filterService, EntityManager entityManager) {
        super(filterService, entityManager, MAGIC_ITEM);
    }

    @Override
    protected OrderSpecifier<?>[] getDefaultOrder() {
        NumberExpression<Integer> rarityRank = Expressions.numberTemplate(
                Integer.class,
                """
                        case {0} \
                         when 'VARIES' then -1\
                         when 'COMMON' then 0\
                         when 'UNCOMMON' then 1\
                         when 'RARE' then 2\
                         when 'VERY_RARE' then 3\
                         when 'LEGENDARY' then 4\
                         when 'ARTIFACT' then 5\
                         when 'UNKNOWN' then 6\
                         else 99 end
                     """,
                MAGIC_ITEM.rarity
        );

        OrderSpecifier<Integer> rarityOrder = new OrderSpecifier<>(Order.ASC, rarityRank);

        return new OrderSpecifier<?>[]{
                rarityOrder,
                MAGIC_ITEM.name.asc()
        };
    }
}

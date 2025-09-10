package club.ttg.dnd5.domain.magic.service;

import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.model.QMagicItem;
import com.querydsl.core.types.OrderSpecifier;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

@Service
public class MagicItemQueryDslSearchService extends AbstractQueryDslSearchService<MagicItem, QMagicItem> {
    private static final QMagicItem MAGIC_ITEM = QMagicItem.magicItem;

    public MagicItemQueryDslSearchService(MagicItemFilterService filterService, EntityManager entityManager) {
        super(filterService, entityManager, MAGIC_ITEM);
    }

    @Override
    protected OrderSpecifier<?>[] getOrder() {
        return new OrderSpecifier[]{
                MAGIC_ITEM.rarity.asc(),
                MAGIC_ITEM.name.asc()
        };
    }
}

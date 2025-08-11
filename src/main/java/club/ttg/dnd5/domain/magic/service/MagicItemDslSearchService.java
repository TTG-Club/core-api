package club.ttg.dnd5.domain.magic.service;

import club.ttg.dnd5.domain.beastiary.model.QCreature;
import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import club.ttg.dnd5.domain.glossary.model.QGlossary;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.model.QMagicItem;
import com.querydsl.core.types.OrderSpecifier;
import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class MagicItemDslSearchService extends AbstractQueryDslSearchService<MagicItem, QMagicItem> {
    private static final QMagicItem MAGIC_ITEM = QMagicItem.magicItem;
    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{MAGIC_ITEM.rarity.asc(), MAGIC_ITEM.name.asc()};

    public MagicItemDslSearchService(MagicItemFilterService magicItemFilterService, EntityManager entityManager) {
        super(magicItemFilterService, entityManager, MAGIC_ITEM);
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
                    ? QGlossary.glossary.name.desc()
                    : QGlossary.glossary.name.asc();
            case "english" -> descending
                    ? QCreature.creature.english.desc()
                    : QGlossary.glossary.english.asc();
            case "category" -> descending
                    ? QGlossary.glossary.tagCategory.desc()
                    : QGlossary.glossary.tagCategory.asc();
            default ->
                // fallback на name
                    QCreature.creature.name.asc();
        };

        return new OrderSpecifier[]{order};
    }
}

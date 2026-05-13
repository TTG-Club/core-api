package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.item.model.QItem;
import club.ttg.dnd5.domain.item.rest.dto.ItemQueryRequest;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ItemPredicateBuilder
{
    private static final QItem Q = QItem.item;

    public BooleanBuilder build(final ItemQueryRequest request)
    {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(Q.isHiddenEntity.isFalse());
        builder.and(PredicateUtils.buildTextSearch(request.getSearch(), Q.name, Q.english, Q.alternative));
        PredicateUtils.applyJsonbEnumArrayFilter(builder, request.getItemType(), "item_types");
        PredicateUtils.applySourcesFilter(builder, request.getSource(), "item", "source");
        PredicateUtils.applyStringFilter(builder, request.getSrdVersion(), Q.srdVersion);
        return builder;
    }
}

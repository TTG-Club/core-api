package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.item.model.QItem;
import club.ttg.dnd5.domain.item.rest.dto.ItemSearchRequest;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import lombok.experimental.UtilityClass;

/**
 * Построитель предикатов QueryDSL для поиска предметов снаряжения.
 * Заменяет 1 legacy FilterGroup (ItemTypeFilterGroup).
 */
@UtilityClass
public class ItemPredicateBuilder
{
    private static final QItem Q = QItem.item;

    public BooleanBuilder build(final ItemSearchRequest request)
    {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(Q.isHiddenEntity.isFalse());

        builder.and(PredicateUtils.buildTextSearch(
                request.getText(),
                Q.name, Q.english, Q.alternative
        ));

        // Тип предмета (JSONB: item_types @> '["WEAPON"]'::jsonb)
        PredicateUtils.applyJsonbEnumArray(builder, request.getItemType(), "item_types");

        // Источники
        PredicateUtils.applySources(builder, request.getEnabledSources(), Q.source.acronym);

        return builder;
    }
}

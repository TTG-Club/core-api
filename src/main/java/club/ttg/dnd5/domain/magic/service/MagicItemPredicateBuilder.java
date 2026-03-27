package club.ttg.dnd5.domain.magic.service;

import club.ttg.dnd5.domain.magic.model.QMagicItem;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemQueryRequest;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MagicItemPredicateBuilder
{
    private static final QMagicItem Q = QMagicItem.magicItem;
    private static final StringPath CATEGORY_PATH = Expressions.stringPath("category");
    private static final StringPath RARITY_PATH = Expressions.stringPath("rarity");

    public BooleanBuilder build(final MagicItemQueryRequest request)
    {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(Q.isHiddenEntity.isFalse());
        builder.and(PredicateUtils.buildTextSearch(request.getSearch(), Q.name, Q.english, Q.alternative));
        PredicateUtils.applyFilterEnum(builder, request.getCategory(), CATEGORY_PATH);
        PredicateUtils.applyFilterEnum(builder, request.getRarity(), RARITY_PATH);
        PredicateUtils.applySingletonFilter(builder, request.getAttunement(),
                "attunement is not null and (attunement->>'required') = 'true'",
                "attunement is null or (attunement->>'required') != 'true'");
        PredicateUtils.applySingletonFilter(builder, request.getCharges(),
                "charges is not null and charges > 0",
                "charges is null or charges <= 0");
        PredicateUtils.applySingletonFilter(builder, request.getCurse(),
                "curse = true",
                "curse = false or curse is null");
        PredicateUtils.applySourcesFilter(builder, request.getSource(), "magicItem", "source");
        return builder;
    }
}

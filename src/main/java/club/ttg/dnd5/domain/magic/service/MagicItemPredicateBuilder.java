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
        if (request.getAttunement() != null && request.getAttunement().isActive())
        {
            if (request.getAttunement().isExclude())
            {
                builder.and(Expressions.booleanTemplate(
                        "attunement is null or (attunement->>'requires') != 'true'"));
            }
            else
            {
                builder.and(Expressions.booleanTemplate(
                        "attunement is not null and (attunement->>'requires') = 'true'"));
            }
        }

        if (request.getCharges() != null && request.getCharges().isActive())
        {
            if (request.getCharges().isExclude())
            {
                builder.and(Expressions.booleanTemplate(
                        "charges is null or charges <= 0"));
            }
            else
            {
                builder.and(Expressions.booleanTemplate(
                        "charges is not null and charges > 0"));
            }
        }

        if (request.getCurse() != null && request.getCurse().isActive())
        {
            if (request.getCurse().isExclude())
            {
                builder.and(Expressions.booleanTemplate(
                        "curse = false or curse is null"));
            }
            else
            {
                builder.and(Expressions.booleanTemplate(
                        "curse = true"));
            }
        }
        PredicateUtils.applySourcesFilter(builder, request.getSource(), "magicItem", "source");
        return builder;
    }
}

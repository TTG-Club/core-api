package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.feat.model.QFeat;
import club.ttg.dnd5.domain.feat.rest.dto.FeatQueryRequest;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FeatPredicateBuilder
{
    private static final QFeat Q = QFeat.feat;
    private static final StringPath CATEGORY_PATH = Expressions.stringPath("category");

    public BooleanBuilder build(final FeatQueryRequest request)
    {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(Q.isHiddenEntity.isFalse());
        builder.and(PredicateUtils.buildTextSearch(request.getSearch(), Q.name, Q.english, Q.alternative));
        PredicateUtils.applyFilterEnum(builder, request.getCategory(), CATEGORY_PATH);
        PredicateUtils.applyJsonbEnumArrayFilter(builder, request.getAbility(), "abilities");
        PredicateUtils.applySingletonFilter(builder, request.getRepeatability(), Q.repeatability);
        PredicateUtils.applySourcesFilter(builder, request.getSource(), "feat", "source");
        return builder;
    }
}

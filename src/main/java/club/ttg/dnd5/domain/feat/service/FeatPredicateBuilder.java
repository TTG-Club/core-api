package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.feat.model.QFeat;
import club.ttg.dnd5.domain.feat.rest.dto.FeatSearchRequest;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import lombok.experimental.UtilityClass;

/**
 * Построитель предикатов QueryDSL для поиска черт.
 * Заменяет 3 legacy FilterGroup.
 */
@UtilityClass
public class FeatPredicateBuilder
{
    private static final QFeat Q = QFeat.feat;
    private static final StringPath CATEGORY_PATH = Expressions.stringPath("category");

    public BooleanBuilder build(final FeatSearchRequest request)
    {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(Q.isHiddenEntity.isFalse());

        builder.and(PredicateUtils.buildTextSearch(
                request.getText(),
                Q.name, Q.english, Q.alternative
        ));

        // Категория (enum STRING)
        PredicateUtils.applyThreeStateEnum(builder, request.getCategory(), CATEGORY_PATH);

        // Характеристики (JSONB-массив)
        PredicateUtils.applyJsonbEnumArray(builder, request.getAbility(), "abilities");

        // Повторяемость
        PredicateUtils.applySingleton(builder, request.getRepeatability(), Q.repeatability);

        // Источники
        PredicateUtils.applySources(builder, request.getEnabledSources(), Q.source.acronym);

        return builder;
    }
}

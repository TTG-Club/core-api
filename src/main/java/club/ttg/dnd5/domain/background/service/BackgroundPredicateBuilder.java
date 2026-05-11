package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.background.model.QBackground;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundQueryRequest;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BackgroundPredicateBuilder
{
    private static final QBackground Q = QBackground.background;

    public BooleanBuilder build(final BackgroundQueryRequest request)
    {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(Q.isHiddenEntity.isFalse());
        builder.and(PredicateUtils.buildTextSearch(request.getSearch(), Q.name, Q.english, Q.alternative));
        PredicateUtils.applyJsonbEnumArrayFilter(builder, request.getAbility(), "abilities");
        PredicateUtils.applyJsonbEnumArrayFilter(builder, request.getSkill(), "skill_proficiencies");
        PredicateUtils.applySourcesFilter(builder, request.getSource(), "background", "source");
        PredicateUtils.applyStringFilter(builder, request.getSrdVersion(), Q.srdVersion);
        return builder;
    }
}

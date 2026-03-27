package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.character_class.model.QCharacterClass;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassQueryRequest;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ClassPredicateBuilder
{
    private static final QCharacterClass Q = QCharacterClass.characterClass;
    private static final StringPath HIT_DICE_PATH = Expressions.stringPath("hit_dice");

    public BooleanBuilder build(final ClassQueryRequest request)
    {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(Q.isHiddenEntity.isFalse());
        builder.and(PredicateUtils.buildTextSearch(request.getSearch(), Q.name, Q.english, Q.alternative));
        PredicateUtils.applyFilterEnum(builder, request.getHitDice(), HIT_DICE_PATH);
        PredicateUtils.applySourcesFilter(builder, request.getSource(), "characterClass", "source");
        return builder;
    }
}

package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.character_class.model.QCharacterClass;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassSearchRequest;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import lombok.experimental.UtilityClass;

/**
 * Построитель предикатов QueryDSL для поиска классов.
 * Заменяет 1 legacy FilterGroup (HitDiceFilterGroup).
 */
@UtilityClass
public class ClassPredicateBuilder
{
    private static final QCharacterClass Q = QCharacterClass.characterClass;
    private static final StringPath HIT_DICE_PATH = Expressions.stringPath("hit_dice");

    public BooleanBuilder build(final ClassSearchRequest request)
    {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(Q.isHiddenEntity.isFalse());

        builder.and(PredicateUtils.buildTextSearch(
                request.getText(),
                Q.name, Q.english, Q.alternative
        ));

        // Кость хитов (enum as STRING column)
        PredicateUtils.applyThreeStateEnum(builder, request.getHitDice(), HIT_DICE_PATH);

        // Источники
        PredicateUtils.applySources(builder, request.getEnabledSources(), Q.source.acronym);

        return builder;
    }
}

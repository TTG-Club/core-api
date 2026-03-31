package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.character_class.model.QCharacterClass;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassQueryRequest;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import lombok.experimental.UtilityClass;

import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class ClassPredicateBuilder
{
    private static final QCharacterClass Q = QCharacterClass.characterClass;

    public BooleanBuilder build(final ClassQueryRequest request)
    {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(Q.isHiddenEntity.isFalse());
        builder.and(PredicateUtils.buildTextSearch(request.getSearch(), Q.name, Q.english, Q.alternative));
        PredicateUtils.applyFilterEnum(
                builder, 
                request.getHitDice(), 
                Q.hitDice, 
                Dice.class
        );
        PredicateUtils.applySourcesFilter(builder, request.getSource(), "characterClass", "source");
        return builder;
    }
}

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
        applyEnumOrdinalFilter(
                builder,
                request.getHitDice(),
                Expressions.numberPath(Integer.class, Q, "hit_dice"),
                Dice.class
        );
        PredicateUtils.applySourcesFilter(builder, request.getSource(), "characterClass", "source");
        return builder;
    }

    public void applyEnumOrdinalFilter(
            BooleanBuilder builder,
            QueryFilter<?> filter,
            NumberPath<Integer> path,
            Class<? extends Enum<?>> enumClass)
    {
        if (filter == null || !filter.isActive())
        {
            return;
        }

        Set<Integer> ordinals = filter.getValues().stream()
                .map(v -> {
                    if (v instanceof String str)
                    {
                        return Enum.valueOf((Class) enumClass, str).ordinal();
                    }
                    return ((Enum<?>) v).ordinal();
                })
                .collect(Collectors.toSet());

        if (filter.isExclude())
        {
            builder.and(path.notIn(ordinals));
        }
        else
        {
            builder.and(path.in(ordinals));
        }
    }
}

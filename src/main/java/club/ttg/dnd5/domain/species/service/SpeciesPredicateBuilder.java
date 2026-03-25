package club.ttg.dnd5.domain.species.service;

import club.ttg.dnd5.domain.species.model.QSpecies;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesSearchRequest;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import lombok.experimental.UtilityClass;

/**
 * Построитель предикатов QueryDSL для поиска видов (рас).
 * Заменяет 1 legacy FilterGroup (CreatureTypeFilterGroup).
 */
@UtilityClass
public class SpeciesPredicateBuilder
{
    private static final QSpecies Q = QSpecies.species;
    private static final StringPath TYPE_PATH = Expressions.stringPath("type");

    public BooleanBuilder build(final SpeciesSearchRequest request)
    {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(Q.isHiddenEntity.isFalse());

        builder.and(PredicateUtils.buildTextSearch(
                request.getText(),
                Q.name, Q.english, Q.alternative
        ));

        // Тип существа (enum STRING)
        PredicateUtils.applyThreeStateEnum(builder, request.getCreatureType(), TYPE_PATH);

        // Источники
        PredicateUtils.applySources(builder, request.getEnabledSources(), Q.source.acronym);

        return builder;
    }
}

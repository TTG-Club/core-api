package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.background.model.QBackground;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundSearchRequest;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import lombok.experimental.UtilityClass;

/**
 * Построитель предикатов QueryDSL для поиска предысторий.
 * Заменяет 2 legacy FilterGroup.
 */
@UtilityClass
public class BackgroundPredicateBuilder
{
    private static final QBackground Q = QBackground.background;

    public BooleanBuilder build(final BackgroundSearchRequest request)
    {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(Q.isHiddenEntity.isFalse());

        builder.and(PredicateUtils.buildTextSearch(
                request.getText(),
                Q.name, Q.english, Q.alternative
        ));

        // Характеристики (JSONB: abilities @> '["STRENGTH"]'::jsonb)
        PredicateUtils.applyJsonbEnumArray(builder, request.getAbility(), "abilities");

        // Навыки (JSONB: skill_proficiencies @> '["ATHLETICS"]'::jsonb)
        PredicateUtils.applyJsonbEnumArray(builder, request.getSkill(), "skill_proficiencies");

        // Источники
        PredicateUtils.applySources(builder, request.getEnabledSources(), Q.source.acronym);

        return builder;
    }
}

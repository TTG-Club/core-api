package club.ttg.dnd5.domain.glossary.service;

import club.ttg.dnd5.domain.glossary.model.QGlossary;
import club.ttg.dnd5.domain.glossary.rest.dto.GlossarySearchRequest;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import lombok.experimental.UtilityClass;

/**
 * Построитель предикатов QueryDSL для поиска глоссария.
 * Заменяет 1 legacy FilterGroup (GlossaryTagCategoryFilterGroup).
 */
@UtilityClass
public class GlossaryPredicateBuilder
{
    private static final QGlossary Q = QGlossary.glossary;

    public BooleanBuilder build(final GlossarySearchRequest request)
    {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(Q.isHiddenEntity.isFalse());

        builder.and(PredicateUtils.buildTextSearch(
                request.getText(),
                Q.name, Q.english, Q.alternative
        ));

        // Категория тега (String поле)
        PredicateUtils.applyThreeState(builder, request.getTagCategory(), Q.tagCategory);

        // Источники — Glossary не имеет source, пропускаем

        return builder;
    }
}

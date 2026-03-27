package club.ttg.dnd5.domain.glossary.service;

import club.ttg.dnd5.domain.glossary.model.QGlossary;
import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryQueryRequest;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GlossaryPredicateBuilder
{
    private static final QGlossary Q = QGlossary.glossary;

    public BooleanBuilder build(final GlossaryQueryRequest request)
    {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(Q.isHiddenEntity.isFalse());

        builder.and(PredicateUtils.buildTextSearch(
                request.getSearch(),
                Q.name, Q.english, Q.alternative
        ));

        // Категория тега (String поле)
        PredicateUtils.applyFilter(builder, request.getTagCategory(), Q.tagCategory);

        return builder;
    }
}

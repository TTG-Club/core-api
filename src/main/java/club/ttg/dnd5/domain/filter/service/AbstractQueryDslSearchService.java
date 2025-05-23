package club.ttg.dnd5.domain.filter.service;

import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanTemplate;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLTemplates;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

import static club.ttg.dnd5.dto.base.filters.Filter.TRUE_EXPRESSION;

@RequiredArgsConstructor
public abstract class AbstractQueryDslSearchService<E, Q extends EntityPathBase<E>> {
    protected final AbstractSavedFilterService<?> savedFilterService;
    protected final EntityManager entityManager;
    protected final Q entityPath;
    protected static final SQLTemplates dialect = new PostgreSQLTemplates();

    private static final String FIND_BY_SEARCH_LINE_QUERY = """
               ({0}.name ilike ''%%{1}%%''
               or {0}.english ilike ''%%{1}%%''
               or {0}.alternative ilike ''%%{1}%%''
               or {0}.name ilike ''%%{2}%%''
               or {0}.english ilike ''%%{2}%%''
               or {0}.alternative ilike ''%%{2}%%'')
            """;

    public List<E> search(String searchLine, SearchBody searchBody) {
        BooleanExpression predicate = Optional.ofNullable(searchLine)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(line ->
                        Expressions.booleanTemplate(MessageFormat.format(FIND_BY_SEARCH_LINE_QUERY, entityPath, line,
                                SwitchLayoutUtils.switchLayout(line))))
                .orElse((BooleanTemplate) TRUE_EXPRESSION)
                .and(Optional.ofNullable(searchBody)
                        .map(SearchBody::getFilter)
                        .orElseGet(savedFilterService::getDefaultFilterInfo).getQuery());

        JPASQLQuery<?> query = new JPASQLQuery<Void>(entityManager, dialect);
        return query.select(entityPath).from(entityPath).where(predicate).orderBy(getOrder()).fetch();

    }

    protected abstract OrderSpecifier<?>[] getOrder();
}

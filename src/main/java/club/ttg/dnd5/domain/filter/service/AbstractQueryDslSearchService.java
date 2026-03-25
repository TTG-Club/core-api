package club.ttg.dnd5.domain.filter.service;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceGroupFilter;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import com.querydsl.core.BooleanBuilder;
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
import java.util.stream.Collectors;

import static club.ttg.dnd5.dto.base.filters.Filter.TRUE_EXPRESSION;

@RequiredArgsConstructor
public abstract class AbstractQueryDslSearchService<E, Q extends EntityPathBase<E>>
{
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

    /**
     * Новый метод поиска: принимает готовый предикат от PredicateBuilder,
     * с поддержкой пагинации.
     */
    public List<E> search(final BooleanBuilder predicate, final int page, final int size)
    {
        JPASQLQuery<E> query = new JPASQLQuery<>(entityManager, dialect);

        return query.select(entityPath)
                .from(entityPath)
                .where(predicate)
                .orderBy(getOrder())
                .offset((long) page * size)
                .limit(size)
                .fetch();
    }

    /**
     * @deprecated Используйте {@link #search(BooleanBuilder, int, int)} с PredicateBuilder
     */
    @Deprecated
    public List<E> search(String searchLine, SearchBody searchBody)
    {
        BooleanExpression predicate = Optional.ofNullable(searchLine)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(line ->
                        Expressions.booleanTemplate(MessageFormat.format(
                                FIND_BY_SEARCH_LINE_QUERY,
                                entityPath,
                                line,
                                SwitchLayoutUtils.switchLayout(line)
                        )))
                .orElse((BooleanTemplate) TRUE_EXPRESSION)
                .and(Optional.ofNullable(searchBody)
                        .map(SearchBody::getFilter)
                        .map(FilterInfo::getQuery)
                        .orElse(TRUE_EXPRESSION))
                .and(buildSourcesPredicate(searchBody));

        JPASQLQuery<E> query = new JPASQLQuery<>(entityManager, dialect);

        return query.select(entityPath)
                .from(entityPath)
                .where(predicate)
                .orderBy(getOrder())
                .fetch();
    }

    private BooleanExpression buildSourcesPredicate(SearchBody searchBody)
    {
        return Optional.ofNullable(searchBody)
                .map(SearchBody::getSources)
                .map(this::extractSelectedSourceValues)
                .filter(values -> !values.isEmpty())
                .map(this::buildSourcePredicate)
                .orElse(TRUE_EXPRESSION);
    }

    private List<String> extractSelectedSourceValues(FilterInfo filterInfo)
    {
        return filterInfo.getGroups().stream()
                .map(group -> (AbstractFilterGroup<?, ?>) group)
                .map(AbstractFilterGroup::getFilters)
                .flatMap(List::stream)
                .map(filter -> (SourceGroupFilter.SourceFilterItem) filter)
                .filter(filter -> Boolean.TRUE.equals(filter.getSelected()))
                .map(SourceGroupFilter.SourceFilterItem::getValue)
                .collect(Collectors.toList());
    }

    protected abstract BooleanExpression buildSourcePredicate(List<String> values);

    protected abstract OrderSpecifier<?>[] getOrder();
}
package club.ttg.dnd5.domain.source.rest.dto.filter;


import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import club.ttg.dnd5.dto.base.filters.FilterRegistry;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@FilterRegistry
@JsonTypeName("src")
public class SourceGroupFilter extends AbstractFilterGroup<String, SourceGroupFilter.SpellSourceFilter> {

    private String name;

    private static final StringPath PATH = Expressions.stringPath("source.acronym");

    public SourceGroupFilter(List<SpellSourceFilter> filters, String name) {
        super(filters);
        this.name = name;
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<String> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ? TRUE_EXPRESSION : PATH.in(new HashSet<>(positiveValues));
        Set<String> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ? (TRUE_EXPRESSION) : PATH.notIn(new HashSet<>(negativeValues)));
    }

    @JsonTypeName("src-i")
    public static class SpellSourceFilter extends AbstractFilterItem<String> {
        public SpellSourceFilter(String label, String value) {
            super(label, value, null);
        }
    }
}

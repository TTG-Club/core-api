package club.ttg.dnd5.domain.source.rest.dto.filter;

import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import club.ttg.dnd5.dto.base.filters.FilterRegistry;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@FilterRegistry
@JsonTypeName("src")
public class SourceGroupFilter extends AbstractFilterGroup<String, SourceGroupFilter.SourceFilterItem>
{
    private static final PathBuilder<Object> SPELL = new PathBuilder<>(Object.class, "spell");
    private static final StringPath PATH = SPELL.getString("source");
    private static final String DEFAULT_NAME = "Источники";

    private String name;

    public SourceGroupFilter(List<SourceFilterItem> filters, String name)
    {
        super(filters);
        this.name = name;
    }

    public static SourceGroupFilter getDefault(List<Source> sources)
    {
        List<SourceFilterItem> items = sources == null
                ? List.of()
                : sources.stream()
                .map(source -> new SourceFilterItem(source.getName(), source.getUrl(), true))
                .toList();

        return new SourceGroupFilter(items, DEFAULT_NAME);
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public BooleanExpression getQuery()
    {
        if (isSingular())
        {
            return TRUE_EXPRESSION;
        }

        Set<String> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues)
                ? TRUE_EXPRESSION
                : PATH.in(new HashSet<>(positiveValues));

        Set<String> negativeValues = getNegative();

        return result.and(CollectionUtils.isEmpty(negativeValues)
                ? TRUE_EXPRESSION
                : PATH.notIn(new HashSet<>(negativeValues)));
    }

    @NoArgsConstructor
    @JsonTypeName("src-i")
    public static class SourceFilterItem extends AbstractFilterItem<String>
    {
        public SourceFilterItem(String label, String value, boolean selected)
        {
            super(label, value, selected);
        }
    }
}
package club.ttg.dnd5.domain.source.rest.dto.filter;

import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import club.ttg.dnd5.dto.base.filters.FilterRegistry;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@FilterRegistry
@JsonTypeName("src")
public class SourceGroupFilter extends AbstractFilterGroup<String, SourceGroupFilter.SourceFilterItem>
{
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
        return TRUE_EXPRESSION;
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
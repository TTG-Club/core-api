package club.ttg.dnd5.domain.filter.rest.dto;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceGroupFilter;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.Filter;
import club.ttg.dnd5.dto.base.filters.State;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class FilterMetadataMapper {

    public FilterMetadataResponse map(SearchBody body) {
        return FilterMetadataResponse.builder()
                .sources(mapSources(body.getSources()))
                .filters(mapFilters(body.getFilter()))
                .build();
    }

    private List<FilterMetadataResponse.SourceGroupMeta> mapSources(FilterInfo sources) {
        if (sources == null || sources.getGroups() == null) {
            return List.of();
        }
        return sources.getGroups().stream()
                .filter(SourceGroupFilter.class::isInstance)
                .map(SourceGroupFilter.class::cast)
                .map(group -> FilterMetadataResponse.SourceGroupMeta.builder()
                        .name(group.getName())
                        .key(getKey(group))
                        .values(group.getFilters().stream()
                                .map(item -> FilterMetadataResponse.SourceValueMeta.builder()
                                        .name(item.getName())
                                        .value(item.getValue())
                                        .enabled(item.getSelected() != null ? item.getSelected() : false)
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<FilterMetadataResponse.FilterGroupMeta> mapFilters(FilterInfo filters) {
        if (filters == null || filters.getGroups() == null) {
            return List.of();
        }
        return filters.getGroups().stream()
                .filter(AbstractFilterGroup.class::isInstance)
                .map(g -> (AbstractFilterGroup<?, ?>) g)
                .map(group -> FilterMetadataResponse.FilterGroupMeta.builder()
                        .name(group.getName())
                        .key(getKey(group))
                        .type(isSingletonType(group) ? "singleton" : "threeState")
                        .values(isSingletonType(group) ? null : group.getFilters().stream()
                                .map(item -> FilterMetadataResponse.FilterValueMeta.builder()
                                        .name(item.getName())
                                        .value(item.getValue())
                                        // positive -> true, negative -> false, unchecked -> null
                                        .state(mapState(item.getState()))
                                        .build())
                                .collect(Collectors.toList()))
                        .state(isSingletonType(group) ? getSingletonState(group) : null)
                        .build())
                .collect(Collectors.toList());
    }

    private String getKey(Filter filter) {
        // Try getting key from @JsonTypeName
        JsonTypeName annotation = filter.getClass().getAnnotation(JsonTypeName.class);
        if (annotation != null) {
            return annotation.value();
        }
        return filter.getClass().getSimpleName();
    }

    private Boolean mapState(State state) {
        if (state == State.POSITIVE) return true;
        if (state == State.NEGATIVE) return false;
        return null;
    }

    private boolean isSingletonType(AbstractFilterGroup<?, ?> group) {
        // Simple heuristic: if the group has only one boolean value option or is marked logically singleton
        // Actually, many old singletons like "Ritual" are just typical groups where one item is selected.
        // Let's keep them as threeState with 1 element for now, or detect if they have "isSingleton" property.
        // The frontend can still process threeState with 1 element.
        return false;
    }

    private Boolean getSingletonState(AbstractFilterGroup<?, ?> group) {
        return null;
    }
}

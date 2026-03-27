package club.ttg.dnd5.domain.filter.rest.dto;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceGroupFilter;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.Filter;
import club.ttg.dnd5.dto.base.filters.FilterIdUtils;
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
        return mapSourcesFromFilterInfo(sources);
    }

    /**
     * Маппит {@link FilterInfo} с группами источников в {@code List<SourceGroupMeta>}.
     * Используется FilterService-ами для построения секции sources в {@link FilterMetadataResponse}.
     */
    public List<FilterMetadataResponse.SourceGroupMeta> mapSourcesFromFilterInfo(FilterInfo sources) {
        if (sources == null || sources.getGroups() == null) {
            return List.of();
        }
        return sources.getGroups().stream()
                .filter(SourceGroupFilter.class::isInstance)
                .map(SourceGroupFilter.class::cast)
                .map(group -> FilterMetadataResponse.SourceGroupMeta.builder()
                        .name(group.getName())
                        .key("source")
                        .values(group.getFilters().stream()
                                .map(item -> FilterMetadataResponse.FilterValueMeta.builder()
                                        .id(String.valueOf(item.getValue()))
                                        .name(item.getName())
                                        .value(item.getValue())
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
                        .type(FilterGroupType.FILTER)
                        .supports(SupportsConfig.builder().mode(true).union(true).build())
                        .values(group.getFilters().stream()
                                .map(item -> {
                                    String id = computeId(item.getValue(), item.getName());
                                    return FilterMetadataResponse.FilterValueMeta.builder()
                                            .id(id)
                                            .name(item.getName())
                                            .value(item.getValue())
                                            .build();
                                })
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Вычисляет id для значения фильтра:
     * - Enum / числовое значение → toString()
     * - Строковое значение → short hash
     */
    private String computeId(Object value, String name) {
        if (value == null) {
            return name != null ? FilterIdUtils.shortHash(name) : "";
        }

        if (value instanceof Enum<?> || value instanceof Number) {
            return String.valueOf(value);
        }

        return FilterIdUtils.shortHash(String.valueOf(value));
    }

    private String getKey(Filter filter) {
        JsonTypeName annotation = filter.getClass().getAnnotation(JsonTypeName.class);
        if (annotation != null) {
            return annotation.value();
        }
        return filter.getClass().getSimpleName();
    }
}

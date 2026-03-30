package club.ttg.dnd5.domain.glossary.service;

import club.ttg.dnd5.domain.filter.rest.dto.FilterKeys;
import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryQueryRequest;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper;
import club.ttg.dnd5.domain.filter.rest.dto.SupportsConfig;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta;
import club.ttg.dnd5.domain.glossary.repository.GlossaryFilterRepository;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GlossaryFilterService
{
    private final SourceSavedFilterService sourceSavedFilterService;
    private final GlossaryFilterRepository glossaryFilterRepository;

    public FilterMetadataResponse getFilterMetadata(Set<String> selectedSources)
    {
        List<String> categories = glossaryFilterRepository.findDistinctTagCategories();

        return FilterMetadataResponse.builder()
                .sources(FilterMetadataMapper.mapSourcesFromFilterInfo(sourceSavedFilterService.getDefaultFilterInfo(selectedSources)))
                .filters(List.of(
                        FilterGroupMeta.builder()
                                .key(FilterKeys.keyOf(GlossaryQueryRequest.class, "tagCategory"))
                                .name("Категория тега")
                                .supports(SupportsConfig.builder().mode(true).union(true).build())
                                .values(categories.stream()
                                        .map(v -> FilterValueMeta.builder()
                                                .id(v)
                                                .value(v)
                                                .name(v)
                                                .build())
                                        .toList())
                                .build()
                ))
                .build();
    }
}

package club.ttg.dnd5.domain.glossary.service;

import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta;
import club.ttg.dnd5.domain.glossary.repository.GlossaryFilterRepository;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GlossaryFilterService
{
    private final SourceSavedFilterService sourceSavedFilterService;
    private final GlossaryFilterRepository glossaryFilterRepository;

    public FilterMetadataResponse getFilterMetadata()
    {
        List<String> categories = glossaryFilterRepository.findDistinctTagCategories();

        return FilterMetadataResponse.builder()
                .sources(FilterMetadataMapper.mapSourcesFromFilterInfo(sourceSavedFilterService.getDefaultFilterInfo()))
                .filters(List.of(
                        FilterGroupMeta.builder()
                                .key("tagCategory")
                                .name("Категория тега")
                                .type("filter")
                                .supportsMode(true)
                                .supportsUnion(true)
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

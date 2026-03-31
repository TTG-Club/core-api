package club.ttg.dnd5.domain.magic.service;

import club.ttg.dnd5.domain.filter.rest.dto.FilterKeys;
import club.ttg.dnd5.domain.magic.repository.MagicItemRepository;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemQueryRequest;
import club.ttg.dnd5.domain.common.dictionary.Rarity;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper;
import club.ttg.dnd5.domain.filter.rest.dto.SupportsConfig;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta;
import club.ttg.dnd5.domain.magic.model.MagicItemCategory;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MagicItemFilterService
{
    private final SourceSavedFilterService sourceSavedFilterService;
    private final MagicItemRepository itemRepository;

    public FilterMetadataResponse getFilterMetadata(Set<String> selectedSources)
    {
        return FilterMetadataResponse.builder()
                .filters(buildFilterGroups())
                .sources(buildSourceGroups(selectedSources))
                .build();
    }

    private List<FilterGroupMeta> buildFilterGroups()
    {
        return List.of(
                FilterGroupMeta.builder()
                        .key(FilterKeys.keyOf(MagicItemQueryRequest.class, "category"))
                        .name("Категории")
                        .supports(SupportsConfig.builder().mode(true).union(false).build())
                        .values(Arrays.stream(MagicItemCategory.values())
                                .map(v -> FilterValueMeta.builder()
                                        .id(v.name())
                                        .value(v.name())
                                        .name(v.getName())
                                        .build())
                                .toList())
                        .build(),
                FilterGroupMeta.builder()
                        .key(FilterKeys.keyOf(MagicItemQueryRequest.class, "rarity"))
                        .name("Редкость")
                        .supports(SupportsConfig.builder().mode(true).union(false).build())
                        .values(Arrays.stream(Rarity.values())
                                .map(v -> FilterValueMeta.builder()
                                        .id(v.name())
                                        .value(v.name())
                                        .name(v.getName())
                                        .build())
                                .toList())
                        .build(),
                FilterGroupMeta.builder()
                        .key(FilterKeys.keyOf(MagicItemQueryRequest.class, "attunement"))
                        .name("Настройка")
                        .supports(SupportsConfig.builder().mode(true).union(false).build())
                        .values(List.of(FilterValueMeta.builder()
                                .id("1")
                                .value("1")
                                .name("Требуется")
                                .build()))
                        .build(),
                FilterGroupMeta.builder()
                        .key(FilterKeys.keyOf(MagicItemQueryRequest.class, "charges"))
                        .name("Заряды")
                        .supports(SupportsConfig.builder().mode(true).union(false).build())
                        .values(List.of(FilterValueMeta.builder()
                                .id("1")
                                .value("1")
                                .name("Есть")
                                .build()))
                        .build(),
                FilterGroupMeta.builder()
                        .key(FilterKeys.keyOf(MagicItemQueryRequest.class, "curse"))
                        .name("Проклятие")
                        .supports(SupportsConfig.builder().mode(true).union(false).build())
                        .values(List.of(FilterValueMeta.builder()
                                .id("1")
                                .value("1")
                                .name("Есть")
                                .build()))
                        .build()
        );
    }

    private List<FilterMetadataResponse.SourceGroupMeta> buildSourceGroups(Set<String> selectedSources)
    {
        List<String> usedSourceCodes = itemRepository.findAllUsedSourceCodes();
        var legacySources = sourceSavedFilterService.getDefaultFilterInfo(usedSourceCodes, selectedSources);

        return FilterMetadataMapper.mapSourcesFromFilterInfo(legacySources);
    }
}

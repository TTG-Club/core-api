package club.ttg.dnd5.domain.magic.service;

import club.ttg.dnd5.domain.common.dictionary.Rarity;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta;
import club.ttg.dnd5.domain.magic.model.MagicItemCategory;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MagicItemFilterService
{
    private final SourceSavedFilterService sourceSavedFilterService;

    public FilterMetadataResponse getFilterMetadata()
    {
        return FilterMetadataResponse.builder()
                .sources(FilterMetadataMapper.mapSourcesFromFilterInfo(sourceSavedFilterService.getDefaultFilterInfo()))
                .filters(List.of(
                        FilterGroupMeta.builder()
                                .key("category")
                                .name("Категории")
                                .type("filter")
                                .supportsMode(true)
                                .supportsUnion(true)
                                .values(Arrays.stream(MagicItemCategory.values())
                                        .map(v -> FilterValueMeta.builder()
                                                .id(v.name())
                                                .value(v.name())
                                                .name(v.getName())
                                                .build())
                                        .toList())
                                .build(),
                        FilterGroupMeta.builder()
                                .key("rarity")
                                .name("Редкость")
                                .type("filter")
                                .supportsMode(true)
                                .supportsUnion(true)
                                .values(Arrays.stream(Rarity.values())
                                        .map(v -> FilterValueMeta.builder()
                                                .id(v.name())
                                                .value(v.name())
                                                .name(v.getName())
                                                .build())
                                        .toList())
                                .build(),
                        FilterGroupMeta.builder()
                                .key("attunement")
                                .name("Настройка")
                                .type("singleton")
                                .supportsMode(false)
                                .supportsUnion(false)
                                .build(),
                        FilterGroupMeta.builder()
                                .key("charges")
                                .name("Заряды")
                                .type("singleton")
                                .supportsMode(false)
                                .supportsUnion(false)
                                .build(),
                        FilterGroupMeta.builder()
                                .key("curse")
                                .name("Проклятие")
                                .type("singleton")
                                .supportsMode(false)
                                .supportsUnion(false)
                                .build()
                ))
                .build();
    }
}

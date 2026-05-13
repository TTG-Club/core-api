package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.character_class.repository.ClassRepository;
import club.ttg.dnd5.domain.filter.rest.dto.FilterKeys;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassQueryRequest;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper;
import club.ttg.dnd5.domain.filter.rest.dto.SupportsConfig;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ClassFilterService
{
    private final SourceSavedFilterService sourceSavedFilterService;
    private final ClassRepository classRepository;

    public FilterMetadataResponse getFilterMetadata(Set<String> selectedSources)
    {
        return FilterMetadataResponse.builder()
                .filters(buildFilterGroups())
                .sources(buildSourceGroups(selectedSources))
                .build();
    }

    private List<FilterGroupMeta> buildFilterGroups()
    {
        List<FilterGroupMeta> groups = new ArrayList<>(2);

        groups.add(FilterGroupMeta.builder()
                .key(FilterKeys.keyOf(ClassQueryRequest.class, "hitDice"))
                .name("Кость хитов")
                .supports(SupportsConfig.builder().mode(true).union(false).build())
                .values(Stream.of(Dice.d6, Dice.d8, Dice.d10, Dice.d12)
                        .map(v -> FilterValueMeta.builder()
                                .id(v.name())
                                .value(v.name())
                                .name(v.getName())
                                .build())
                        .toList())
                .build());

        // Версия SRD
        List<String> srdVersions = classRepository.findDistinctSrdVersions();
        if (!srdVersions.isEmpty()) {
            groups.add(FilterGroupMeta.builder()
                    .key(FilterKeys.keyOf(ClassQueryRequest.class, "srdVersion"))
                    .name("Версия SRD")
                    .supports(SupportsConfig.builder().mode(true).union(false).build())
                    .values(srdVersions.stream()
                            .map(v -> FilterValueMeta.builder()
                                    .id(v)
                                    .value(v)
                                    .name("SRD " + v)
                                    .build())
                            .toList())
                    .build());
        }

        return groups;
    }

    private List<FilterMetadataResponse.SourceGroupMeta> buildSourceGroups(Set<String> selectedSources)
    {
        List<String> usedSourceCodes = classRepository.findAllUsedSourceCodes();
        var legacySources = sourceSavedFilterService.getDefaultFilterInfo(usedSourceCodes, selectedSources);

        return FilterMetadataMapper.mapSourcesFromFilterInfo(legacySources);
    }
}

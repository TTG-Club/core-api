package club.ttg.dnd5.domain.bastion.service;

import club.ttg.dnd5.domain.bastion.model.BastionOrder;
import club.ttg.dnd5.domain.bastion.model.BastionRules;
import club.ttg.dnd5.domain.bastion.model.FacilityCategory;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.repository.BastionFacilityRepository;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionFacilityQueryRequest;
import club.ttg.dnd5.domain.filter.rest.dto.FilterKeys;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta;
import club.ttg.dnd5.domain.filter.rest.dto.SupportsConfig;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BastionFacilityFilterService
{
    private final SourceSavedFilterService sourceSavedFilterService;
    private final BastionFacilityRepository facilityRepository;

    public FilterMetadataResponse getFilterMetadata(Set<String> selectedSources)
    {
        return FilterMetadataResponse.builder()
                .filters(buildFilterGroups())
                .sources(buildSourceGroups(selectedSources))
                .build();
    }

    private List<FilterGroupMeta> buildFilterGroups()
    {
        List<FilterGroupMeta> groups = new ArrayList<>(6);

        groups.add(FilterGroupMeta.builder()
                .key(FilterKeys.keyOf(BastionFacilityQueryRequest.class, "category"))
                .name("Вид")
                .supports(SupportsConfig.builder().mode(true).union(false).build())
                .values(Arrays.stream(FacilityCategory.values())
                        .map(v -> FilterValueMeta.builder()
                                .id(v.name())
                                .value(v.name())
                                .name(v.getName())
                                .build())
                        .toList())
                .build());

        groups.add(FilterGroupMeta.builder()
                .key(FilterKeys.keyOf(BastionFacilityQueryRequest.class, "level"))
                .name("Уровень")
                .supports(SupportsConfig.builder().mode(true).union(false).build())
                .values(BastionRules.SPECIAL_FACILITY_PROGRESSION.stream()
                        .map(p -> FilterValueMeta.builder()
                                .id(String.valueOf(p.level()))
                                .value((long) p.level())
                                .name(String.valueOf(p.level()))
                                .build())
                        .toList())
                .build());

        groups.add(FilterGroupMeta.builder()
                .key(FilterKeys.keyOf(BastionFacilityQueryRequest.class, "order"))
                .name("Приказ")
                .supports(SupportsConfig.builder().mode(true).union(true).build())
                .values(Arrays.stream(BastionOrder.values())
                        .filter(v -> !v.isBastionWide())
                        .map(v -> FilterValueMeta.builder()
                                .id(v.name())
                                .value(v.name())
                                .name(v.getName())
                                .build())
                        .toList())
                .build());

        groups.add(FilterGroupMeta.builder()
                .key(FilterKeys.keyOf(BastionFacilityQueryRequest.class, "space"))
                .name("Пространство")
                .supports(SupportsConfig.builder().mode(true).union(false).build())
                .values(Arrays.stream(FacilitySpace.values())
                        .map(v -> FilterValueMeta.builder()
                                .id(v.name())
                                .value(v.name())
                                .name(v.getName())
                                .build())
                        .toList())
                .build());

        groups.add(FilterGroupMeta.builder()
                .key(FilterKeys.keyOf(BastionFacilityQueryRequest.class, "prerequisite"))
                .name("Требование")
                .supports(SupportsConfig.builder().mode(true).union(false).build())
                .values(List.of(FilterValueMeta.builder()
                        .id("1")
                        .value("1")
                        .name("Есть")
                        .build()))
                .build());

        List<String> srdVersions = facilityRepository.findDistinctSrdVersions();
        if (!srdVersions.isEmpty()) {
            groups.add(FilterGroupMeta.builder()
                    .key(FilterKeys.keyOf(BastionFacilityQueryRequest.class, "srdVersion"))
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
        List<String> usedSourceCodes = facilityRepository.findAllUsedSourceCodes();
        var legacySources = sourceSavedFilterService.getDefaultFilterInfo(usedSourceCodes, selectedSources);

        return FilterMetadataMapper.mapSourcesFromFilterInfo(legacySources);
    }
}

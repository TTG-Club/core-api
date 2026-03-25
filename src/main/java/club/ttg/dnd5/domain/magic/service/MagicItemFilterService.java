package club.ttg.dnd5.domain.magic.service;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.magic.repository.MagicItemRepository;

import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MagicItemFilterService extends AbstractSavedFilterService {
    private final MagicItemRepository magicItemRepository;

    public MagicItemFilterService(SourceSavedFilterService sourceSavedFilterService,
                                  MagicItemRepository magicItemRepository) {
        super(sourceSavedFilterService);
        this.magicItemRepository = magicItemRepository;
    }

    // legacy (deprecated)
    @Override
    @Deprecated
    public SearchBody getDefaultFilterInfo()
    {
        return new SearchBody(
                sourceSavedFilterService.getDefaultFilterInfo(),
                buildDefaultFilterInfo()
        );
    }

    @Override
    @Deprecated
    protected FilterInfo buildDefaultFilterInfo() {
        return new FilterInfo(java.util.Collections.emptyList());
    }

    @Override
    public club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse getFilterMetadata() {
        return club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.builder()
                .sources(club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper.map(sourceSavedFilterService.getFilter()).getSources())
                .filters(List.of(
                        club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta.builder()
                                .key("category")
                                .name("Категории")
                                .type("threeState")
                                .values(java.util.Arrays.stream(club.ttg.dnd5.domain.magic.model.MagicItemCategory.values())
                                        .map(v -> club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta.builder()
                                                .name(v.getName()).value(v.name()).build())
                                        .toList())
                                .build(),
                        club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta.builder()
                                .key("rarity")
                                .name("Редкость")
                                .type("threeState")
                                .values(java.util.Arrays.stream(club.ttg.dnd5.domain.common.dictionary.Rarity.values())
                                        .map(v -> club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta.builder()
                                                .name(v.getName()).value(v.name()).build())
                                        .toList())
                                .build(),
                        club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta.builder()
                                .key("other")
                                .name("Прочее")
                                .type("threeState")
                                .values(List.of(
                                        club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta.builder().name("Настройка").value("attunement").build(),
                                        club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta.builder().name("Заряды").value("charges").build(),
                                        club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta.builder().name("Проклятие").value("curse").build(),
                                        club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta.builder().name("Расходуемый").value("consumable").build()
                                ))
                                .build()
                ))
                .build();
    }
}

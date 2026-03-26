package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.model.CreatureCategory;
import club.ttg.dnd5.domain.beastiary.model.filter.CreatureTraitsStats;
import club.ttg.dnd5.domain.beastiary.repository.CreatureRepository;
import club.ttg.dnd5.domain.beastiary.rest.dto.filter.CrFilterGroup;
import club.ttg.dnd5.domain.beastiary.rest.dto.filter.CreatureAlignmentFilterGroup;
import club.ttg.dnd5.domain.beastiary.rest.dto.filter.CreatureHabitatFilterGroup;
import club.ttg.dnd5.domain.beastiary.rest.dto.filter.CreatureOtherFilterGroup;
import club.ttg.dnd5.domain.beastiary.rest.dto.filter.CreatureSensesFilterGroup;
import club.ttg.dnd5.domain.beastiary.rest.dto.filter.CreatureSizeFilterGroup;
import club.ttg.dnd5.domain.beastiary.rest.dto.filter.CreatureTagFilterGroup;
import club.ttg.dnd5.domain.beastiary.rest.dto.filter.CreatureTraitsFilterGroup;
import club.ttg.dnd5.domain.beastiary.rest.dto.filter.CreatureTypeFilterGroup;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CreatureFilterService extends AbstractSavedFilterService
{
    private static final int TOP_TRAITS = 10;

    private final CreatureRepository creatureRepository;

    public CreatureFilterService(
            SourceSavedFilterService sourceSavedFilterService,
            CreatureRepository creatureRepository
    )
    {
        super(sourceSavedFilterService);
        this.creatureRepository = creatureRepository;
    }

    @Override
    @Deprecated
    public SearchBody getDefaultFilterInfo()
    {
        List<String> usedSourceCodes = creatureRepository.findAllUsedSourceCodes();

        return new SearchBody(
                sourceSavedFilterService.getDefaultFilterInfo(usedSourceCodes),
                buildDefaultFilterInfo()
        );
    }

    @Override
    @Deprecated
    protected FilterInfo buildDefaultFilterInfo()
    {
        List<Creature> creatures = creatureRepository.findAll();

        CreatureTraitsStats stats = new CreatureTraitsStats();
        List<String> topTraits = stats.getTopTraits(creatures, TOP_TRAITS);
        List<String> tags = creatures.stream()
                .map(Creature::getTypes)
                .map(CreatureCategory::getText)
                .filter(StringUtils::hasText)
                .map(String::toLowerCase)
                .distinct()
                .sorted()
                .toList();

        return new FilterInfo(List.of(
                CrFilterGroup.getDefault(),
                CreatureTypeFilterGroup.getDefault(),
                CreatureTagFilterGroup.getDefault(tags),
                CreatureSizeFilterGroup.getDefault(),
                CreatureAlignmentFilterGroup.getDefault(),
                CreatureTraitsFilterGroup.getDefault(topTraits),
                CreatureOtherFilterGroup.getDefault(),
                CreatureSensesFilterGroup.getDefault(),
                CreatureHabitatFilterGroup.getDefault()
        ));
    }

    @Override
    public club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse getFilterMetadata() {
        return club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper.map(getDefaultFilterInfo());
    }
}
package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.model.CreatureCategory;
import club.ttg.dnd5.domain.beastiary.model.filter.CreatureSavedFilter;
import club.ttg.dnd5.domain.beastiary.model.filter.CreatureTraitsStats;
import club.ttg.dnd5.domain.beastiary.repository.CreatureRepository;
import club.ttg.dnd5.domain.beastiary.repository.CreatureSavedFilterRepository;
import club.ttg.dnd5.domain.beastiary.rest.dto.filter.*;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CreatureFilterService extends AbstractSavedFilterService<CreatureSavedFilter> {
    private static final String FILTER_VERSION = "1.0";
    private static final int TOP_TRAITS = 10;
    private final CreatureRepository creatureRepository;

    public CreatureFilterService(CreatureSavedFilterRepository creatureSavedFilterRepository,
                                 UserService userService, CreatureRepository creatureRepository) {
        super(creatureSavedFilterRepository, userService);
        this.creatureRepository = creatureRepository;
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo() {
        List<Creature> creatures = creatureRepository.findAll();

        CreatureTraitsStats stats = new CreatureTraitsStats();
        List<String> topTraits = stats.getTopTraits(creatures, TOP_TRAITS);
        List<String> tags = creatures.stream()
                .map(Creature::getTypes)
                .map(CreatureCategory::getText)
                .filter(StringUtils::hasText)
                .map(String::toLowerCase)
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
        ), FILTER_VERSION);
    }
}

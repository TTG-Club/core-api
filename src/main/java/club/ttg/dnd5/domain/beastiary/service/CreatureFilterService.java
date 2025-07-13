package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.model.enumus.CreatureTraits;
import club.ttg.dnd5.domain.beastiary.model.filter.CreatureSavedFilter;
import club.ttg.dnd5.domain.beastiary.model.filter.CreatureTraitsStats;
import club.ttg.dnd5.domain.beastiary.repository.CreatureRepository;
import club.ttg.dnd5.domain.beastiary.repository.CreatureSavedFilterRepository;
import club.ttg.dnd5.domain.beastiary.rest.dto.filter.*;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreatureFilterService extends AbstractSavedFilterService<CreatureSavedFilter> {
    private static final String FILTER_VERSION = "1.0";

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
        List<CreatureTraits> top4Traits = stats.getTopTraits(creatures, 4);

        return new FilterInfo(List.of(
                CrFilterGroup.getDefault(),
                CreatureSizeFilterGroup.getDefault(),
                AlignmentFilterGroup.getDefault(),
                CreatureTypesFilterGroup.getDefault(),
                CreatureSensesFilterGroup.getDefault(),
                CreatureTraitsFilterGroup.getDefault(top4Traits),
                CreatureSectionFilterGroup.getDefault()
        ), FILTER_VERSION);
    }
}

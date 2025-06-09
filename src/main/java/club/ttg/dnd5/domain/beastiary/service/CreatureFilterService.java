package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.model.filter.CreatureSavedFilter;
import club.ttg.dnd5.domain.beastiary.repository.CreatureSavedFilterRepository;
import club.ttg.dnd5.domain.beastiary.rest.dto.filter.CrFilterGroup;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreatureFilterService extends AbstractSavedFilterService<CreatureSavedFilter> {
    private static final String FILTER_VERSION = "1.0";

    public CreatureFilterService(CreatureSavedFilterRepository creatureSavedFilterRepository,
                                 UserService userService) {
        super(creatureSavedFilterRepository, userService);
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo() {
        return new FilterInfo(List.of(
                CrFilterGroup.getDefault()
        ), FILTER_VERSION);
    }
}

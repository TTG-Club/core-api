package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.background.model.filter.BackgroundSavedFilter;
import club.ttg.dnd5.domain.background.repository.BackgroundSavedFilterRepository;
import club.ttg.dnd5.domain.background.rest.dto.filter.AbilityFilterGroup;
import club.ttg.dnd5.domain.background.rest.dto.filter.SkillFilterGroup;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BackgroundFilterService extends AbstractSavedFilterService<BackgroundSavedFilter> {
    private static final String FILTER_VERSION = "1.0";

    public BackgroundFilterService(BackgroundSavedFilterRepository savedFilterRepository,
                                   UserService userService) {
        super(savedFilterRepository, userService);
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo() {
        return new FilterInfo(List.of(
                AbilityFilterGroup.getDefault(),
                SkillFilterGroup.getDefault()
        ), FILTER_VERSION);
    }
}

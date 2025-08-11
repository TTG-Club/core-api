package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.beastiary.model.filter.CreatureSavedFilter;
import club.ttg.dnd5.domain.beastiary.repository.CreatureSavedFilterRepository;
import club.ttg.dnd5.domain.feat.rest.dto.filter.FeatCategoryFilterGroup;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeatFilterService extends AbstractSavedFilterService<CreatureSavedFilter> {
    private static final String FILTER_VERSION = "1.0";

    public FeatFilterService(CreatureSavedFilterRepository creatureSavedFilterRepository,
                             UserService userService) {
        super(creatureSavedFilterRepository, userService);
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo() {


        return new FilterInfo(List.of(
                FeatCategoryFilterGroup.getDefault()
        ), FILTER_VERSION);
    }
}

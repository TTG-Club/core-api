package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.feat.model.filter.FeatSavedFilter;
import club.ttg.dnd5.domain.feat.repository.FeatSavedFilterRepository;
import club.ttg.dnd5.domain.feat.rest.dto.filter.FeatCategoryFilterGroup;
import club.ttg.dnd5.domain.feat.rest.dto.filter.FeatOtherFilterGroup;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeatFilterService extends AbstractSavedFilterService<FeatSavedFilter> {
    private static final String FILTER_VERSION = "1.0";

    public FeatFilterService(FeatSavedFilterRepository featSavedFilterRepository,
                             UserService userService) {
        super(featSavedFilterRepository, userService);
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo() {
        return new FilterInfo(List.of(
                FeatCategoryFilterGroup.getDefault(),
                FeatOtherFilterGroup.getDefault()
        ), FILTER_VERSION);
    }
}

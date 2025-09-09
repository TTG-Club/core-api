package club.ttg.dnd5.domain.magic.service;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.magic.model.filter.MagicItemSavedFilter;
import club.ttg.dnd5.domain.magic.repository.MagicItemSavedFilterRepository;
import club.ttg.dnd5.domain.magic.rest.dto.filter.AttunementFilterGroup;
import club.ttg.dnd5.domain.magic.rest.dto.filter.MagicItemCategoryFilterGroup;
import club.ttg.dnd5.domain.magic.rest.dto.filter.RarityFilterGroup;
import club.ttg.dnd5.domain.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MagicItemFilterService extends AbstractSavedFilterService<MagicItemSavedFilter> {
    private static final String FILTER_VERSION = "1.0";

    public MagicItemFilterService(MagicItemSavedFilterRepository savedFilterRepository,
                                  UserService userService) {
        super(savedFilterRepository, userService);
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo() {
        return new FilterInfo(List.of(
                MagicItemCategoryFilterGroup.getDefault(),
                RarityFilterGroup.getDefault(),
                AttunementFilterGroup.getDefault()
        ), FILTER_VERSION);
    }
}

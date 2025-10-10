package club.ttg.dnd5.domain.item.rest.mapper;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.item.model.filter.ItemSavedFilter;
import club.ttg.dnd5.domain.item.repository.ItemSavedFilterRepository;
import club.ttg.dnd5.domain.item.rest.dto.filter.ItemTypeFilterGroup;
import club.ttg.dnd5.domain.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemFilterService extends AbstractSavedFilterService<ItemSavedFilter> {
    private static final String FILTER_VERSION = "1.0";

    public ItemFilterService(ItemSavedFilterRepository savedFilterRepository,
                             UserService userService) {
        super(savedFilterRepository, userService);
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo() {
        return new FilterInfo(List.of(
                ItemTypeFilterGroup.getDefault()
        ), FILTER_VERSION);
    }
}

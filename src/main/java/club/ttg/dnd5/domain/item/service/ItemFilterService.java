package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.item.rest.dto.filter.ItemTypeFilterGroup;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemFilterService extends AbstractSavedFilterService {


    public ItemFilterService(SourceSavedFilterService sourceSavedFilterService) {
        super(sourceSavedFilterService);
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo() {
        return new FilterInfo(List.of(
                ItemTypeFilterGroup.getDefault()
        ));
    }
}

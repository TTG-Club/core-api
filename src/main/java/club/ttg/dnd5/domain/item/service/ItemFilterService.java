package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import club.ttg.dnd5.domain.item.rest.dto.filter.ItemTypeFilterGroup;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemFilterService extends AbstractSavedFilterService {
    private final ItemRepository itemRepository;

    public ItemFilterService(SourceSavedFilterService sourceSavedFilterService,
                             ItemRepository itemRepository) {
        super(sourceSavedFilterService);
        this.itemRepository = itemRepository;
    }

    @Override
    public SearchBody getDefaultFilterInfo()
    {
        List<String> usedSourceCodes = itemRepository.findAllUsedSourceCodes();

        return new SearchBody(
                sourceSavedFilterService.getDefaultFilterInfo(usedSourceCodes),
                buildDefaultFilterInfo()
        );
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo() {
        return new FilterInfo(List.of(
                ItemTypeFilterGroup.getDefault()
        ));
    }
}

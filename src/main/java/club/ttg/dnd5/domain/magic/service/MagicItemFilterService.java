package club.ttg.dnd5.domain.magic.service;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.magic.rest.dto.filter.MagicItemCategoryFilterGroup;
import club.ttg.dnd5.domain.magic.rest.dto.filter.MagicItemOtherFilterGroup;
import club.ttg.dnd5.domain.magic.rest.dto.filter.RarityFilterGroup;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MagicItemFilterService extends AbstractSavedFilterService {


    public MagicItemFilterService(SourceSavedFilterService sourceSavedFilterService) {
        super(sourceSavedFilterService);
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo() {
        return new FilterInfo(List.of(
                MagicItemCategoryFilterGroup.getDefault(),
                RarityFilterGroup.getDefault(),
                MagicItemOtherFilterGroup.getDefault()));
    }
}

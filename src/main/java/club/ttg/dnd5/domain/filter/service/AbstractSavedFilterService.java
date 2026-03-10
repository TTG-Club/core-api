package club.ttg.dnd5.domain.filter.service;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.filter.model.SourceFilterInfo;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractSavedFilterService {

    protected final SourceSavedFilterService sourceSavedFilterService;


    public SearchBody getDefaultFilterInfo() {
        SourceFilterInfo sourceFilterInfo = sourceSavedFilterService.getDefaultFilterInfo();
        FilterInfo filter = buildDefaultFilterInfo();
        return new SearchBody(sourceFilterInfo, filter);

    }

    protected abstract FilterInfo buildDefaultFilterInfo();
}

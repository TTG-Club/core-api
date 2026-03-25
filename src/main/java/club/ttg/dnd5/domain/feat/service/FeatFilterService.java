package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.domain.feat.rest.dto.filter.FeatAbilityFilterGroup;
import club.ttg.dnd5.domain.feat.rest.dto.filter.FeatCategoryFilterGroup;
import club.ttg.dnd5.domain.feat.rest.dto.filter.FeatOtherFilterGroup;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeatFilterService extends AbstractSavedFilterService  {
    private final FeatRepository featRepository;

    public FeatFilterService(SourceSavedFilterService sourceSavedFilterService,
                             FeatRepository featRepository) {
        super(sourceSavedFilterService);
        this.featRepository = featRepository;
    }

    @Override
    public SearchBody getDefaultFilterInfo()
    {
        List<String> usedSourceCodes = featRepository.findAllUsedSourceCodes();

        return new SearchBody(
                sourceSavedFilterService.getDefaultFilterInfo(usedSourceCodes),
                buildDefaultFilterInfo()
        );
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo() {
        return new FilterInfo(List.of(
                FeatCategoryFilterGroup.getDefault(),
                FeatAbilityFilterGroup.getDefault(),
                FeatOtherFilterGroup.getDefault()
        ));
    }
}

package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.background.repository.BackgroundRepository;
import club.ttg.dnd5.domain.background.rest.dto.filter.AbilityFilterGroup;
import club.ttg.dnd5.domain.background.rest.dto.filter.SkillFilterGroup;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BackgroundFilterService extends AbstractSavedFilterService {
    private final BackgroundRepository backgroundRepository;

    public BackgroundFilterService(SourceSavedFilterService sourceSavedFilterService,
                                  BackgroundRepository backgroundRepository) {
        super(sourceSavedFilterService);
        this.backgroundRepository = backgroundRepository;
    }

    @Override
    public SearchBody getDefaultFilterInfo()
    {
        List<String> usedSourceCodes = backgroundRepository.findAllUsedSourceCodes();

        return new SearchBody(
                sourceSavedFilterService.getDefaultFilterInfo(usedSourceCodes),
                buildDefaultFilterInfo()
        );
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo() {
        return new FilterInfo(List.of(
                AbilityFilterGroup.getDefault(),
                SkillFilterGroup.getDefault()
        ));
    }
}

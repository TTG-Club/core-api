package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.character_class.repository.ClassRepository;
import club.ttg.dnd5.domain.character_class.rest.dto.filter.HitDiceFilterGroup;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ClassFilterService extends AbstractSavedFilterService
{
    private final SourceSavedFilterService sourceSavedFilterService;
    private final ClassRepository classRepository;

    public ClassFilterService(
            SourceSavedFilterService sourceSavedFilterService,
            ClassRepository classRepository
    )
    {
        super(sourceSavedFilterService);
        this.sourceSavedFilterService = sourceSavedFilterService;
        this.classRepository = classRepository;
    }

    @Override
    public SearchBody getDefaultFilterInfo()
    {
        List<String> usedSourceCodes = classRepository.findAllUsedSourceCodes();

        return new SearchBody(
                sourceSavedFilterService.getDefaultFilterInfo(usedSourceCodes),
                buildDefaultFilterInfo()
        );
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo()
    {
        return new FilterInfo(List.of(
                HitDiceFilterGroup.getDefault()
        ));
    }
}
package club.ttg.dnd5.domain.species.service;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import club.ttg.dnd5.domain.species.repository.SpeciesRepository;
import club.ttg.dnd5.domain.species.rest.dto.filter.CreatureTypeFilterGroup;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpeciesFilterService extends AbstractSavedFilterService
{
    private final SourceSavedFilterService sourceSavedFilterService;
    private final SpeciesRepository speciesRepository;

    public SpeciesFilterService(
            SourceSavedFilterService sourceSavedFilterService,
            SpeciesRepository speciesRepository
    )
    {
        super(sourceSavedFilterService);
        this.sourceSavedFilterService = sourceSavedFilterService;
        this.speciesRepository = speciesRepository;
    }

    @Override
    public SearchBody getDefaultFilterInfo()
    {
        List<String> usedSourceCodes = speciesRepository.findAllUsedSourceCodes();

        return new SearchBody(
                sourceSavedFilterService.getDefaultFilterInfo(usedSourceCodes),
                buildDefaultFilterInfo()
        );
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo()
    {
        return new FilterInfo(List.of(
                CreatureTypeFilterGroup.getDefault()
        ));
    }
}
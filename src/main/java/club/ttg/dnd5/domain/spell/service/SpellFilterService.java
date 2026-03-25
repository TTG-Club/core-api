package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.character_class.service.ClassService;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.spell.rest.dto.filter.SpellCastingTimeFilterRange;
import club.ttg.dnd5.domain.spell.rest.dto.filter.SpellClassFilterGroup;
import club.ttg.dnd5.domain.spell.rest.dto.filter.SpellComponentsFilterGroup;
import club.ttg.dnd5.domain.spell.rest.dto.filter.SpellConditionFilterGroup;
import club.ttg.dnd5.domain.spell.rest.dto.filter.SpellDamageTypeFilterGroup;
import club.ttg.dnd5.domain.spell.rest.dto.filter.SpellDistanceFilterRange;
import club.ttg.dnd5.domain.spell.rest.dto.filter.SpellDurationFilterRange;
import club.ttg.dnd5.domain.spell.rest.dto.filter.SpellHealingTypeFilterGroup;
import club.ttg.dnd5.domain.spell.rest.dto.filter.SpellLevelFilterGroup;
import club.ttg.dnd5.domain.spell.rest.dto.filter.SpellOtherFilterGroup;
import club.ttg.dnd5.domain.spell.rest.dto.filter.SpellSavingThrowFilterGroup;
import club.ttg.dnd5.domain.spell.rest.dto.filter.SpellSchoolFilterGroup;
import club.ttg.dnd5.domain.spell.rest.dto.filter.SpellSubclassFilterGroup;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpellFilterService extends AbstractSavedFilterService
{
    private final ClassService classService;
    private final SourceSavedFilterService sourceSavedFilterService;
    private final SpellRepository spellRepository;

    public SpellFilterService(
            SourceSavedFilterService sourceSavedFilterService,
            ClassService classService,
            SpellRepository spellRepository
    )
    {
        super(sourceSavedFilterService);
        this.classService = classService;
        this.sourceSavedFilterService = sourceSavedFilterService;
        this.spellRepository = spellRepository;
    }

    @Override
    @Deprecated
    public SearchBody getDefaultFilterInfo()
    {
        List<String> usedSourceCodes = spellRepository.findAllUsedSourceCodes();

        return new SearchBody(
                sourceSavedFilterService.getDefaultFilterInfo(usedSourceCodes),
                buildDefaultFilterInfo()
        );
    }

    @Override
    @Deprecated
    protected FilterInfo buildDefaultFilterInfo()
    {
        return new FilterInfo(List.of(
                SpellClassFilterGroup.getDefault(
                        classService.findAllMagicClasses()
                ),
                SpellSubclassFilterGroup.getDefault(
                        classService.findAllMagicSubclasses()
                ),
                SpellLevelFilterGroup.getDefault(),
                SpellSchoolFilterGroup.getDefault(),
                SpellOtherFilterGroup.getDefault(),
                SpellDamageTypeFilterGroup.getDefault(),
                SpellHealingTypeFilterGroup.getDefault(),
                SpellSavingThrowFilterGroup.getDefault(),
                SpellCastingTimeFilterRange.getDefault(),
                SpellDistanceFilterRange.getDefault(),
                SpellDurationFilterRange.getDefault(),
                SpellComponentsFilterGroup.getDefault(),
                SpellConditionFilterGroup.getDefault()
        ));
    }

    @Override
    public club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse getFilterMetadata() {
        return club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper.map(getDefaultFilterInfo());
    }
}
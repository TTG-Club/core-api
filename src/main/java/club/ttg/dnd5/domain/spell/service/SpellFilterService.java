package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.character_class.service.ClassService;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import club.ttg.dnd5.domain.spell.rest.dto.filter.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpellFilterService extends AbstractSavedFilterService {
    private final ClassService classService;

    public SpellFilterService(SourceSavedFilterService sourceSavedFilterService, ClassService classService) {
        super(sourceSavedFilterService);
        this.classService = classService;
    }


    @Override
    protected FilterInfo buildDefaultFilterInfo() {
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
}

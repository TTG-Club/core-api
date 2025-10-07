package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.character_class.service.ClassService;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.spell.model.filter.SpellSavedFilter;
import club.ttg.dnd5.domain.spell.repository.SpellSavedFilterRepository;
import club.ttg.dnd5.domain.spell.rest.dto.filter.*;
import club.ttg.dnd5.domain.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpellFilterService extends AbstractSavedFilterService<SpellSavedFilter> {
    private static final String FILTER_VERSION = "1.0";
    private final ClassService classService;

    public SpellFilterService(ClassService classService,
            SpellSavedFilterRepository spellSavedFilterRepository,
            UserService userService) {
        super(spellSavedFilterRepository, userService);
        this.classService = classService;
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo() {
        return new FilterInfo(List.of(
                SpellClassFilterGroup.getDefault(
                        classService.findAllClasses(null , "name")
                ),
                SpellSubclassFilterGroup.getDefault(
                        classService.findAllSubclasses(null , "name")
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
                SpellComponentsFilterGroup.getDefault()
        ), FILTER_VERSION);
    }
}

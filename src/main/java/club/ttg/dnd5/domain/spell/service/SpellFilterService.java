package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.filter.model.FilterDto;
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

    public SpellFilterService(SpellSavedFilterRepository spellSavedFilterRepository, UserService userService) {
        super(spellSavedFilterRepository, userService);
    }

    @Override
    protected FilterDto buildDefaultFilterDto() {
        return new FilterDto(List.of(
                SpellLevelFilterGroup.getDefault(),
                SpellSchoolFilterGroup.getDefault(),
                SpellRitualFilterSingleton.getDefault(),
                SpellConcentrationFilterSingleton.getDefault(),
                SpellDamageTypeFilterGroup.getDefault(),
                SpellHealingTypeFilterGroup.getDefault(),
                SpellSavingThrowFilterGroup.getDefault(),
                SpellCastingTimeFilterRange.getDefault(),
                SpellDistanceFilterRange.getDefault(),
                SpellDurationFilterRange.getDefault(),
                SpellComponentsFilterGroup.getDefault(),
                SpellUpcastableFilterSingleton.getDefault()
        ), FILTER_VERSION);
    }
}

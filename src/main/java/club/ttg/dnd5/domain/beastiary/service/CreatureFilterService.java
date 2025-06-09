package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.rest.dto.filter.CrFilterGroup;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.spell.model.filter.SpellSavedFilter;
import club.ttg.dnd5.domain.spell.repository.SpellSavedFilterRepository;
import club.ttg.dnd5.domain.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreatureFilterService extends AbstractSavedFilterService<SpellSavedFilter> {
    private static final String FILTER_VERSION = "1.0";

    public CreatureFilterService(SpellSavedFilterRepository spellSavedFilterRepository, UserService userService) {
        super(spellSavedFilterRepository, userService);
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo() {
        return new FilterInfo(List.of(
                CrFilterGroup.getDefault()
        ), FILTER_VERSION);
    }
}

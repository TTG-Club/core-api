package club.ttg.dnd5.domain.filter.service;

import club.ttg.dnd5.domain.filter.model.AbstractSavedFilter;
import club.ttg.dnd5.domain.filter.model.FilterDto;
import club.ttg.dnd5.domain.filter.repository.AbstractSavedFilterRepository;
import club.ttg.dnd5.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractSavedFilterService<T extends AbstractSavedFilter>{
    protected final AbstractSavedFilterRepository<T> savedFilterRepository;
    protected final UserService userService;


    public FilterDto getDefaultFilterDto(){
        return userService.getCurrentUserId()
                .flatMap(savedFilterRepository::findByUserIdAndDefaultFilterTrue)
                .map(T::getFilter)
                .orElseGet(this::buildDefaultFilterDto);
    }


    protected abstract FilterDto buildDefaultFilterDto();
}

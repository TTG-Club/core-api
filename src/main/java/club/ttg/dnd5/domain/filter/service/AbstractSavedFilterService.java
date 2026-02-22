package club.ttg.dnd5.domain.filter.service;

import club.ttg.dnd5.domain.filter.model.AbstractSavedFilter;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.repository.SavedFilterRepository;
import club.ttg.dnd5.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public abstract class AbstractSavedFilterService<T extends AbstractSavedFilter> {
    protected final SavedFilterRepository<T> savedFilterRepository;
    protected final UserService userService;

    public T save(T entity) {
        return savedFilterRepository.save(entity);
    }

    public Optional<T> findSavedFilter() {
        return userService.getCurrentUserId()
                .flatMap(savedFilterRepository::findByUserIdAndDefaultFilterTrue);
    }

    public FilterInfo getDefaultFilterInfo() {
        return userService.getCurrentUserId()
                .flatMap(savedFilterRepository::findByUserIdAndDefaultFilterTrue)
                .map(T::getFilter)
                .orElseGet(this::buildDefaultFilterInfo);
    }

    protected abstract FilterInfo buildDefaultFilterInfo();
}

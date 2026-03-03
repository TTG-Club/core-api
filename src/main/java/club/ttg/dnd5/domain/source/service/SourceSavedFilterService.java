package club.ttg.dnd5.domain.source.service;

import club.ttg.dnd5.domain.filter.model.SourceFilterInfo;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.model.SourceType;
import club.ttg.dnd5.domain.source.model.filter.SourceSavedFilter;
import club.ttg.dnd5.domain.source.repository.SourceSavedFilterRepository;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceGroupFilter;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceSavedFilterResponse;
import club.ttg.dnd5.domain.source.rest.mapper.SavedSourceFilterMapper;
import club.ttg.dnd5.domain.user.service.UserService;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SourceSavedFilterService {

    private final SourceService sourceService;
    private final SavedSourceFilterMapper savedSourceFilterMapper;
    private final SourceSavedFilterRepository sourceSavedFilterRepository;
    private final UserService userService;

    public Optional<SourceSavedFilter> findSavedFilter() {
        return userService.getCurrentUserId()
                .flatMap(sourceSavedFilterRepository::findByUserIdAndDefaultFilterTrue);
    }

    public SourceSavedFilter getSavedFilter() {
        return findSavedFilter()
                .map(this::updateToActualAndSave)
                .orElseGet(this::createActualAndSave);
    }

    public SourceSavedFilterResponse getSavedFilterResponse() {
        return savedSourceFilterMapper.toResponse(getSavedFilter());
    }

    private SourceSavedFilter updateToActualAndSave(SourceSavedFilter sourceSavedFilter) {
        Map<String, Boolean> sourceMap = sourceSavedFilter.getFilter().getGroups().stream()
                .map(SourceGroupFilter::getFilters)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(SourceGroupFilter.SourceFilterItem::getValue, SourceGroupFilter.SourceFilterItem::getSelected));

        SourceFilterInfo filter = buildDefaultFilterInfo();

        filter.getGroups().stream()
                .map(SourceGroupFilter::getFilters)
                .flatMap(Collection::stream)
                .forEach(item -> item.setSelected(sourceMap.get(item.getValue())));

        sourceSavedFilter.setFilter(filter);
        return save(sourceSavedFilter);
    }

    private SourceSavedFilter createActualAndSave() {
        UUID userId = userService.getCurrentUserId().get();
        return save(savedSourceFilterMapper.toEntity(buildDefaultFilterInfo(), userId));
    }

    public SourceSavedFilter save(SourceSavedFilter entity) {
        return sourceSavedFilterRepository.save(entity);
    }

    public SourceSavedFilterResponse createFilter(SourceFilterInfo filter) {
        if (findSavedFilter().isEmpty()) {
            return userService.getCurrentUserId()
                    .map(uuid -> savedSourceFilterMapper.toEntity(filter, uuid))
                    .map(this::save)
                    .map(savedSourceFilterMapper::toResponse)
                    .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
        } else {
            throw new EntityExistException("Фильр для пользователя уже существует");
        }
    }

    public SourceSavedFilterResponse updateFilter(SourceFilterInfo filter) {
        return findSavedFilter()
                .map(saved -> savedSourceFilterMapper.update(saved, filter))
                .map(savedSourceFilterMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Фильр для пользователя не существует"));
    }

    protected SourceFilterInfo buildDefaultFilterInfo() {
        Map<SourceType, List<Source>> sourceMap = sourceService.findAll().stream()
                .collect(Collectors.groupingBy(Source::getType));
        return new SourceFilterInfo(Arrays.stream(SourceType.values())
                .filter(sourceMap::containsKey)
                .map(type -> new SourceGroupFilter(
                        sourceMap.get(type).stream()
                                .map(src -> new SourceGroupFilter.SourceFilterItem(src.getName(), src.getAcronym()))
                                .toList(),
                        type.getName()
                )).collect(Collectors.toList()));
    }

    public SourceFilterInfo getDefaultFilterInfo() {
        return getSavedFilter().getFilter();
    }
}

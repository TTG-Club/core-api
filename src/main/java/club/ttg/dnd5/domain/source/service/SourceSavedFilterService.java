package club.ttg.dnd5.domain.source.service;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.filter.model.SourceFilterInfo;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.model.SourceOrigin;
import club.ttg.dnd5.domain.source.model.filter.SourceSavedFilter;
import club.ttg.dnd5.domain.source.repository.SourceSavedFilterRepository;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceGroupFilter;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceSavedFilterRequest;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceSavedFilterResponse;
import club.ttg.dnd5.domain.source.rest.mapper.SavedSourceFilterMapper;
import club.ttg.dnd5.domain.user.service.UserService;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SourceSavedFilterService
{
    private final SourceService sourceService;
    private final SavedSourceFilterMapper savedSourceFilterMapper;
    private final SourceSavedFilterRepository sourceSavedFilterRepository;
    private final UserService userService;

    public Optional<SourceSavedFilter> findSavedFilter()
    {
        return userService.getCurrentUserId()
                .flatMap(sourceSavedFilterRepository::findByUserIdAndDefaultFilterTrue);
    }

    public SourceSavedFilter getSavedFilter()
    {
        return findSavedFilter()
                .map(this::updateToActualAndSave)
                .orElseGet(this::createActualAndSave);
    }

    public Set<String> getSavedSources()
    {
        return findSavedFilter()
            .map(SourceSavedFilter::getFilter)
            .map(FilterInfo::getGroups)
            .stream()
            .flatMap(Collection::stream)
            .filter(SourceGroupFilter.class::isInstance)
            .map(SourceGroupFilter.class::cast)
            .flatMap(group -> group.getFilters().stream())
            .filter(filter -> Boolean.TRUE.equals(filter.getSelected()))
            .map(AbstractFilterItem::getValue)
            .collect(Collectors.collectingAndThen(
                    Collectors.toSet(),
                    result -> result.isEmpty() ? getAllSourceAcronyms() : result
            ));
    }

    private Set<String> getAllSourceAcronyms()
    {
        return sourceService.findAll()
                .stream()
                .map(Source::getAcronym)
                .collect(Collectors.toSet());
    }

    public SourceSavedFilterResponse getSavedFilterResponse()
    {
        return savedSourceFilterMapper.toResponse(getSavedFilter());
    }

    private SourceSavedFilter updateToActualAndSave(SourceSavedFilter sourceSavedFilter)
    {
        Map<String, Boolean> sourceMap = new HashMap<>();

        sourceSavedFilter.getFilter().getGroups().stream()
                .filter(SourceGroupFilter.class::isInstance)
                .map(SourceGroupFilter.class::cast)
                .map(SourceGroupFilter::getFilters)
                .flatMap(Collection::stream)
                .forEach(item -> sourceMap.put(item.getValue(), item.getSelected()));

        SourceSavedFilterRequest filter = buildDefaultFilterInfo();

        filter.getFilter().getGroups().stream()
                .filter(SourceGroupFilter.class::isInstance)
                .map(SourceGroupFilter.class::cast)
                .map(SourceGroupFilter::getFilters)
                .flatMap(Collection::stream)
                .forEach(item -> item.setSelected(sourceMap.get(item.getValue())));

        sourceSavedFilter.setFilter(filter.getFilter());
        return save(sourceSavedFilter);
    }

    private SourceSavedFilter createActualAndSave()
    {
        UUID userId = userService.getCurrentUserId().orElseThrow(() -> new EntityExistException("ID пользователя не найден"));
        return save(savedSourceFilterMapper.toEntity(buildDefaultFilterInfo(), userId));
    }

    public SourceSavedFilter save(SourceSavedFilter entity)
    {
        return sourceSavedFilterRepository.save(entity);
    }

    public SourceSavedFilterResponse createFilter(SourceSavedFilterRequest filter)
    {
        if (findSavedFilter().isEmpty())
        {
            return userService.getCurrentUserId()
                    .map(uuid -> savedSourceFilterMapper.toEntity(filter, uuid))
                    .map(this::save)
                    .map(savedSourceFilterMapper::toResponse)
                    .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
        }

        throw new EntityExistException("Фильтр для пользователя уже существует");
    }

    @Transactional
    public SourceSavedFilterResponse updateFilter(SourceSavedFilterRequest filter)
    {
        var savedFilter = findSavedFilter();
        return savedFilter
                .map(saved -> savedSourceFilterMapper.update(saved, filter))
                .map(savedSourceFilterMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Фильтр для пользователя не существует"));
    }

    protected SourceSavedFilterRequest buildDefaultFilterInfo()
    {
        return buildDefaultFilterInfoFromSources(sourceService.findAll());
    }

    protected SourceSavedFilterRequest buildDefaultFilterInfo(List<String> sourceCodes)
    {
        if (sourceCodes == null || sourceCodes.isEmpty())
        {
            return new SourceSavedFilterRequest();
        }

        Set<String> allowedCodes = sourceCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (allowedCodes.isEmpty())
        {
            return new SourceSavedFilterRequest();
        }

        List<Source> filteredSources = sourceService.findAll().stream()
                .filter(source -> allowedCodes.contains(source.getAcronym()))
                .toList();

        return buildDefaultFilterInfoFromSources(filteredSources);
    }

    private SourceSavedFilterRequest buildDefaultFilterInfoFromSources(List<Source> sources)
    {
        if (sources == null || sources.isEmpty())
        {
            return SourceSavedFilterRequest.builder()
                    .filter(new SourceFilterInfo(Collections.emptyList()))
                    .build();
        }

        Map<SourceOrigin, List<Source>> sourceMap = sources.stream()
                .filter(source -> source != null && source.getOrigin() != null)
                .collect(Collectors.groupingBy(Source::getOrigin));

        SourceFilterInfo filterInfo = new SourceFilterInfo(
                Arrays.stream(SourceOrigin.values())
                        .filter(sourceMap::containsKey)
                        .map(origin -> new SourceGroupFilter(
                                sourceMap.get(origin).stream()
                                        .sorted((left, right) -> left.getName().compareToIgnoreCase(right.getName()))
                                        .map(src -> new SourceGroupFilter.SourceFilterItem(
                                                src.getName(),
                                                src.getAcronym(),
                                                true
                                        ))
                                        .collect(Collectors.toList()),
                                origin.getName(),
                                origin
                        ))
                        .collect(Collectors.toList())
        );

        return SourceSavedFilterRequest.builder()
                .filter(filterInfo)
                .build();
    }

    public SourceFilterInfo getDefaultFilterInfo()
    {
        return getDefaultFilterInfo(Set.of());
    }

    public SourceFilterInfo getDefaultFilterInfo(Set<String> selectedSources)
    {
        if (userService.getCurrentUserId().isPresent())
        {
            return new SourceFilterInfo(getSavedFilter().getFilter().getGroups());
        }

        SourceSavedFilterRequest defaultFilter = buildDefaultFilterInfo();

        if (!selectedSources.isEmpty())
        {
            applySelection(defaultFilter, selectedSources);
        }

        return new SourceFilterInfo(defaultFilter.getFilter().getGroups());
    }

    public SourceFilterInfo getDefaultFilterInfo(List<String> sourceCodes, Set<String> selectedSources)
    {
        SourceSavedFilterRequest actualFilter = buildDefaultFilterInfo(sourceCodes);

        if (userService.getCurrentUserId().isEmpty())
        {
            if (!selectedSources.isEmpty())
            {
                applySelection(actualFilter, selectedSources);
            }

            return new SourceFilterInfo(actualFilter.getFilter().getGroups());
        }

        Optional<SourceSavedFilter> savedFilterOptional = findSavedFilter();

        if (savedFilterOptional.isEmpty())
        {
            return new SourceFilterInfo(actualFilter.getFilter().getGroups());
        }

        Map<String, Boolean> selectedMap = new HashMap<>();

        savedFilterOptional.get().getFilter().getGroups().stream()
                .filter(SourceGroupFilter.class::isInstance)
                .map(SourceGroupFilter.class::cast)
                .map(SourceGroupFilter::getFilters)
                .flatMap(Collection::stream)
                .forEach(item -> selectedMap.put(item.getValue(), item.getSelected()));

        actualFilter.getFilter().getGroups().stream()
                .filter(SourceGroupFilter.class::isInstance)
                .map(SourceGroupFilter.class::cast)
                .map(SourceGroupFilter::getFilters)
                .flatMap(Collection::stream)
                .forEach(item -> item.setSelected(selectedMap.get(item.getValue())));

        return new SourceFilterInfo(actualFilter.getFilter().getGroups());
    }

    /**
     * Применяет выборку из GET-параметров: selected=true для источников из selectedSources,
     * selected=null для остальных.
     */
    private void applySelection(SourceSavedFilterRequest filter, Set<String> selectedSources)
    {
        filter.getFilter().getGroups().stream()
                .filter(SourceGroupFilter.class::isInstance)
                .map(SourceGroupFilter.class::cast)
                .map(SourceGroupFilter::getFilters)
                .flatMap(Collection::stream)
                .forEach(item -> item.setSelected(
                        selectedSources.contains(item.getValue()) ? true : null
                ));
    }

    public SearchBody getFilter() {
        return new SearchBody(getDefaultFilterInfo(), getDefaultFilterInfo());
    }
}

package club.ttg.dnd5.domain.source.service;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.filter.model.SourceFilterInfo;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.model.SourceType;
import club.ttg.dnd5.domain.source.model.filter.SourceSavedFilter;
import club.ttg.dnd5.domain.source.repository.SourceSavedFilterRepository;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceGroupFilter;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceSavedFilterRequest;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceSavedFilterResponse;
import club.ttg.dnd5.domain.source.rest.mapper.SavedSourceFilterMapper;
import club.ttg.dnd5.domain.user.service.UserService;
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

    /**
     * Источники, включённые у пользователя. Книга, которой ещё нет в сохранённом
     * фильтре (добавлена после его сохранения), считается включённой.
     */
    public Set<String> getSavedSources()
    {
        Map<String, Boolean> savedSelection = findSavedFilter()
                .map(this::collectSelection)
                .orElseGet(Map::of);

        Set<String> selected = savedSelection.entrySet().stream()
                .filter(entry -> Boolean.TRUE.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        Set<String> allAcronyms = getAllSourceAcronyms();

        if (selected.isEmpty())
        {
            return allAcronyms;
        }

        allAcronyms.stream()
                .filter(acronym -> !savedSelection.containsKey(acronym))
                .forEach(selected::add);

        return selected;
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
        SourceSavedFilterRequest filter = buildDefaultFilterInfo();

        applySavedSelection(filter, collectSelection(sourceSavedFilter));

        sourceSavedFilter.setFilter(filter.getFilter());
        return save(sourceSavedFilter);
    }

    /**
     * Выбор пользователя из сохранённого фильтра: акроним источника → включён ли он.
     */
    private Map<String, Boolean> collectSelection(SourceSavedFilter sourceSavedFilter)
    {
        Map<String, Boolean> selection = new HashMap<>();

        Optional.ofNullable(sourceSavedFilter.getFilter())
                .map(FilterInfo::getGroups)
                .stream()
                .flatMap(Collection::stream)
                .filter(SourceGroupFilter.class::isInstance)
                .map(SourceGroupFilter.class::cast)
                .map(SourceGroupFilter::getFilters)
                .flatMap(Collection::stream)
                .forEach(item -> selection.put(item.getValue(), item.getSelected()));

        return selection;
    }

    /**
     * Переносит выбор пользователя на актуальный список источников. Источник, которого
     * в сохранённом фильтре ещё не было (новая книга), остаётся включённым.
     */
    private void applySavedSelection(SourceSavedFilterRequest filter, Map<String, Boolean> savedSelection)
    {
        filter.getFilter().getGroups().stream()
                .filter(SourceGroupFilter.class::isInstance)
                .map(SourceGroupFilter.class::cast)
                .map(SourceGroupFilter::getFilters)
                .flatMap(Collection::stream)
                .filter(item -> savedSelection.containsKey(item.getValue()))
                .forEach(item -> item.setSelected(savedSelection.get(item.getValue())));
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

    /**
     * Фильтр источников по книгам, которые встречаются в разделе.
     *
     * <p>Раздел без записей (новый, ещё не наполненный) получает пустой фильтр, а не
     * запрос без фильтра: вызывающие сразу читают {@code getFilter().getGroups()}, и
     * {@code null} ронял метаданные фильтров всего раздела.</p>
     */
    protected SourceSavedFilterRequest buildDefaultFilterInfo(List<String> sourceCodes)
    {
        if (sourceCodes == null || sourceCodes.isEmpty())
        {
            return buildDefaultFilterInfoFromSources(List.of());
        }

        Set<String> allowedCodes = sourceCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (allowedCodes.isEmpty())
        {
            return buildDefaultFilterInfoFromSources(List.of());
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

        Map<SourceType, List<Source>> sourceMap = sources.stream()
                .filter(source -> source != null && source.getType() != null)
                .collect(Collectors.groupingBy(Source::getType));

        SourceFilterInfo filterInfo = new SourceFilterInfo(
                Arrays.stream(SourceType.values())
                        .filter(sourceMap::containsKey)
                        .map(type -> new SourceGroupFilter(
                                sourceMap.get(type).stream()
                                        .sorted((left, right) -> left.getName().compareToIgnoreCase(right.getName()))
                                        .map(src -> new SourceGroupFilter.SourceFilterItem(
                                                src.getName(),
                                                src.getAcronym(),
                                                true
                                        ))
                                        .collect(Collectors.toList()),
                                type.getName(),
                                type
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

    public SourceFilterInfo getDefaultFilterInfo(List<String> sourceCodes)
    {
        return getDefaultFilterInfo(sourceCodes, Set.of());
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

        applySavedSelection(actualFilter, collectSelection(savedFilterOptional.get()));

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
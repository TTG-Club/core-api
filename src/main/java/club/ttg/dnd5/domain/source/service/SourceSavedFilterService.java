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

import java.util.Arrays;
import java.util.Collection;
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

        SourceFilterInfo filter = buildDefaultFilterInfo();

        filter.getGroups().stream()
                .filter(SourceGroupFilter.class::isInstance)
                .map(SourceGroupFilter.class::cast)
                .map(SourceGroupFilter::getFilters)
                .flatMap(Collection::stream)
                .forEach(item -> item.setSelected(sourceMap.get(item.getValue())));

        sourceSavedFilter.setFilter(filter);
        return save(sourceSavedFilter);
    }

    private SourceSavedFilter createActualAndSave()
    {
        UUID userId = userService.getCurrentUserId().get();
        return save(savedSourceFilterMapper.toEntity(buildDefaultFilterInfo(), userId));
    }

    public SourceSavedFilter save(SourceSavedFilter entity)
    {
        return sourceSavedFilterRepository.save(entity);
    }

    public SourceSavedFilterResponse createFilter(SourceFilterInfo filter)
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

    public SourceSavedFilterResponse updateFilter(SourceFilterInfo filter)
    {
        return findSavedFilter()
                .map(saved -> savedSourceFilterMapper.update(saved, filter))
                .map(savedSourceFilterMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Фильтр для пользователя не существует"));
    }

    protected SourceFilterInfo buildDefaultFilterInfo()
    {
        return buildDefaultFilterInfoFromSources(sourceService.findAll());
    }

    protected SourceFilterInfo buildDefaultFilterInfo(List<String> sourceCodes)
    {
        if (sourceCodes == null || sourceCodes.isEmpty())
        {
            return new SourceFilterInfo(List.of());
        }

        Set<String> allowedCodes = sourceCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (allowedCodes.isEmpty())
        {
            return new SourceFilterInfo(List.of());
        }

        List<Source> filteredSources = sourceService.findAll().stream()
                .filter(source -> allowedCodes.contains(source.getAcronym()))
                .toList();

        return buildDefaultFilterInfoFromSources(filteredSources);
    }

    private SourceFilterInfo buildDefaultFilterInfoFromSources(List<Source> sources)
    {
        Map<SourceType, List<Source>> sourceMap = sources.stream()
                .collect(Collectors.groupingBy(Source::getType));

        return new SourceFilterInfo(
                Arrays.stream(SourceType.values())
                        .filter(sourceMap::containsKey)
                        .map(type -> new SourceGroupFilter(
                                sourceMap.get(type).stream()
                                        .sorted((left, right) -> left.getName().compareToIgnoreCase(right.getName()))
                                        .map(src -> new SourceGroupFilter.SourceFilterItem(
                                                src.getName(),
                                                src.getAcronym(),
                                                true)
                                        )
                                        .toList(),
                                type.getName()
                        ))
                        .collect(Collectors.toList())
        );
    }

    public SourceFilterInfo getDefaultFilterInfo()
    {
        if (userService.getCurrentUserId().isPresent())
        {
            return (SourceFilterInfo) getSavedFilter().getFilter();
        }

        return buildDefaultFilterInfo();
    }

    public SourceFilterInfo getDefaultFilterInfo(List<String> sourceCodes)
    {
        SourceFilterInfo actualFilter = buildDefaultFilterInfo(sourceCodes);

        if (userService.getCurrentUserId().isEmpty())
        {
            return actualFilter;
        }

        Optional<SourceSavedFilter> savedFilterOptional = findSavedFilter();

        if (savedFilterOptional.isEmpty())
        {
            return actualFilter;
        }

        Map<String, Boolean> selectedMap = new HashMap<>();

        savedFilterOptional.get().getFilter().getGroups().stream()
                .filter(SourceGroupFilter.class::isInstance)
                .map(SourceGroupFilter.class::cast)
                .map(SourceGroupFilter::getFilters)
                .flatMap(Collection::stream)
                .forEach(item -> selectedMap.put(item.getValue(), item.getSelected()));

        actualFilter.getGroups().stream()
                .filter(SourceGroupFilter.class::isInstance)
                .map(SourceGroupFilter.class::cast)
                .map(SourceGroupFilter::getFilters)
                .flatMap(Collection::stream)
                .forEach(item -> item.setSelected(selectedMap.get(item.getValue())));

        return actualFilter;
    }
}
package club.ttg.dnd5.domain.source.service;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SourceFilterInfo;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.model.SourceType;
import club.ttg.dnd5.domain.source.model.filter.SourceSavedFilter;
import club.ttg.dnd5.domain.source.repository.SourceSavedFilterRepository;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceGroupFilter;
import club.ttg.dnd5.domain.source.rest.mapper.SavedSourceFilterMapper;
import club.ttg.dnd5.domain.user.service.UserService;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SourceSavedFilterServiceTest {
    private static final UUID USER_ID = UUID.randomUUID();

    private final UserService userService = mock(UserService.class);
    private final SourceService sourceService = mock(SourceService.class);
    private final SourceSavedFilterRepository repository = mock(SourceSavedFilterRepository.class);
    private final SourceSavedFilterService service = new SourceSavedFilterService(
            sourceService,
            mock(SavedSourceFilterMapper.class),
            repository,
            userService);

    /**
     * Книга, добавленная после того, как пользователь сохранил фильтр, должна появиться
     * у него включённой. Выключенные им книги при этом остаются выключенными.
     */
    @Test
    void newSourceIsEnabledInSavedFilter() {
        givenUserWithSavedFilter(Map.of("PHB", true, "DMG", false));
        when(sourceService.findAll()).thenReturn(List.of(source("PHB"), source("DMG"), source("NEW")));
        when(repository.save(any(SourceSavedFilter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(Map.of("PHB", true, "DMG", false, "NEW", true), selection(service.getSavedFilter().getFilter()));
    }

    @Test
    void newSourceIsEnabledInSectionFilter() {
        givenUserWithSavedFilter(Map.of("PHB", true, "DMG", false));
        when(sourceService.findAll()).thenReturn(List.of(source("PHB"), source("DMG"), source("NEW")));

        SourceFilterInfo info = service.getDefaultFilterInfo(List.of("PHB", "DMG", "NEW"), Set.of());

        assertEquals(Map.of("PHB", true, "DMG", false, "NEW", true), selection(info));
    }

    /**
     * Списки разделов читают сохранённый фильтр как есть, без пересборки, поэтому
     * новую книгу надо досчитать включённой и здесь.
     */
    @Test
    void newSourceIsAmongSavedSources() {
        givenUserWithSavedFilter(Map.of("PHB", true, "DMG", false));
        when(sourceService.findAll()).thenReturn(List.of(source("PHB"), source("DMG"), source("NEW")));

        assertEquals(Set.of("PHB", "NEW"), service.getSavedSources());
    }

    private void givenUserWithSavedFilter(Map<String, Boolean> savedSelection) {
        List<SourceGroupFilter.SourceFilterItem> items = savedSelection.entrySet().stream()
                .map(entry -> new SourceGroupFilter.SourceFilterItem(entry.getKey(), entry.getKey(), entry.getValue()))
                .toList();

        SourceSavedFilter saved = new SourceSavedFilter();
        saved.setFilter(new SourceFilterInfo(List.of(new SourceGroupFilter(items, "Базовые"))));

        when(userService.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        when(repository.findByUserIdAndDefaultFilterTrue(USER_ID)).thenReturn(Optional.of(saved));
    }

    private static Source source(String acronym) {
        Source source = new Source();
        source.setAcronym(acronym);
        source.setName(acronym);
        source.setType(SourceType.OFFICIAL);
        return source;
    }

    private static Map<String, Boolean> selection(FilterInfo info) {
        return info.getGroups().stream()
                .map(SourceGroupFilter.class::cast)
                .flatMap(group -> group.getFilters().stream())
                .collect(Collectors.toMap(AbstractFilterItem::getValue, AbstractFilterItem::getSelected));
    }

    /**
     * У раздела без записей нет ни одной книги. Метаданные фильтров должны отдать пустой
     * фильтр источников, а не упасть: так было у новых «Бастионов».
     */
    @Test
    void sectionWithoutRecordsGetsEmptySourceFilter() {
        when(userService.getCurrentUserId()).thenReturn(Optional.empty());

        SourceFilterInfo info = service.getDefaultFilterInfo(List.of(), Set.of());

        assertTrue(info.getGroups().isEmpty());
    }

    @Test
    void blankSourceCodesAreTreatedAsNoSources() {
        when(userService.getCurrentUserId()).thenReturn(Optional.empty());

        SourceFilterInfo info = service.getDefaultFilterInfo(List.of(" "), Set.of());

        assertTrue(info.getGroups().isEmpty());
    }
}

package club.ttg.dnd5.domain.source.service;

import club.ttg.dnd5.domain.filter.model.SourceFilterInfo;
import club.ttg.dnd5.domain.source.repository.SourceSavedFilterRepository;
import club.ttg.dnd5.domain.source.rest.mapper.SavedSourceFilterMapper;
import club.ttg.dnd5.domain.user.service.UserService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SourceSavedFilterServiceTest {
    private final UserService userService = mock(UserService.class);
    private final SourceSavedFilterService service = new SourceSavedFilterService(
            mock(SourceService.class),
            mock(SavedSourceFilterMapper.class),
            mock(SourceSavedFilterRepository.class),
            userService);

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

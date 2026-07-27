package club.ttg.dnd5.domain.user.service;

import club.ttg.dnd5.domain.user.model.UserHandle;
import club.ttg.dnd5.domain.user.repository.UserHandleRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserHandleServiceTest {

    private final UserHandleRepository repository = mock(UserHandleRepository.class);
    private final UserHandleService service = new UserHandleService(repository);

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void resolveHandle_existing_returnsFrozenWithoutSaving() {
        when(repository.findById(USER_ID)).thenReturn(Optional.of(new UserHandle(USER_ID, "magistrus")));

        assertEquals("magistrus", service.resolveHandle(USER_ID, "SomethingElse"));
        verify(repository, never()).save(any());
    }

    @Test
    void resolveHandle_new_slugifiesLogin() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());
        when(repository.existsByHandleIgnoreCase("magistrus")).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertEquals("magistrus", service.resolveHandle(USER_ID, "Magistrus"));
    }

    @Test
    void resolveHandle_collision_appendsNumericSuffix() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());
        when(repository.existsByHandleIgnoreCase("magistrus")).thenReturn(true);
        when(repository.existsByHandleIgnoreCase("magistrus-2")).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertEquals("magistrus-2", service.resolveHandle(USER_ID, "Magistrus"));
    }

    @Test
    void resolveHandle_cyrillicLogin_transliterates() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());
        when(repository.existsByHandleIgnoreCase(any())).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // транслитерация кириллицы даёт непустой латинский slug
        String handle = service.resolveHandle(USER_ID, "Магистр");
        assertEquals("magistr", handle);
    }

    @Test
    void resolveHandle_emptyAfterSlug_usesFallback() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());
        when(repository.existsByHandleIgnoreCase(any())).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertEquals("user", service.resolveHandle(USER_ID, "!!!"));
    }
}

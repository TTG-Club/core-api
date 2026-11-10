package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.vttg.model.VttgCompendiumVersion;
import club.ttg.dnd5.domain.vttg.repository.VttgCompendiumVersionRepository;
import club.ttg.dnd5.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VttgCompendiumVersionServiceTest {

    private final VttgCompendiumVersionRepository repository = mock(VttgCompendiumVersionRepository.class);
    private final VttgCompendiumVersionService service =
            new VttgCompendiumVersionService(repository, mock(PlatformTransactionManager.class));

    @Test
    void currentReadsDatabaseOnceAndKeepsInMemory() {
        storedVersion(35);

        assertEquals(35, service.current());
        assertEquals(35, service.current());

        verify(repository, times(1)).findById(VttgCompendiumVersion.SINGLE_ROW_ID);
    }

    @Test
    void raiseToGreaterVersionUpdatesMemoryWithoutRereading() {
        storedVersion(35);
        service.current();
        when(repository.raise(eq(VttgCompendiumVersion.SINGLE_ROW_ID), eq(36), any(), eq("admin"))).thenReturn(1);

        VttgCompendiumVersionService.Snapshot raised = service.raise(36, "admin");

        assertEquals(36, raised.version());
        assertEquals("admin", raised.updatedBy());
        assertEquals(36, service.current());
        verify(repository, times(1)).findById(VttgCompendiumVersion.SINGLE_ROW_ID);
    }

    @Test
    void raiseToVersionNotGreaterThanCurrentIsConflict() {
        storedVersion(36);
        // Условие «version < :new» в UPDATE не выполнилось — ни одной строки не обновлено.
        when(repository.raise(anyShort(), anyInt(), any(), anyString())).thenReturn(0);

        ApiException conflict = assertThrows(ApiException.class, () -> service.raise(36, "admin"));

        assertEquals(HttpStatus.CONFLICT, conflict.getStatus());
        assertEquals("Версия должна быть больше текущей (36)", conflict.getMessage());
    }

    @Test
    void lateReadDoesNotRollBackFresherWrite() {
        storedVersion(35);
        service.current();
        when(repository.raise(anyShort(), anyInt(), any(), anyString())).thenReturn(1);
        service.raise(36, "admin");
        // Значение в памяти «протухло», а база (реплика/запоздавшее чтение) ещё отдаёт старое.
        expireSnapshot();

        assertEquals(36, service.current(), "версия только растёт — запоздавшее чтение не откатывает запись");
    }

    private void storedVersion(int version) {
        VttgCompendiumVersion row = new VttgCompendiumVersion();
        ReflectionTestUtils.setField(row, "id", VttgCompendiumVersion.SINGLE_ROW_ID);
        ReflectionTestUtils.setField(row, "version", version);
        when(repository.findById(VttgCompendiumVersion.SINGLE_ROW_ID)).thenReturn(Optional.of(row));
    }

    private void expireSnapshot() {
        VttgCompendiumVersionService.Snapshot current =
                (VttgCompendiumVersionService.Snapshot) ReflectionTestUtils.getField(service, "snapshot");
        ReflectionTestUtils.setField(service, "snapshot", new VttgCompendiumVersionService.Snapshot(
                current.version(), current.updatedAt(), current.updatedBy(),
                System.nanoTime() - VttgCompendiumVersionService.REFRESH_INTERVAL_NANOS - 1));
    }
}

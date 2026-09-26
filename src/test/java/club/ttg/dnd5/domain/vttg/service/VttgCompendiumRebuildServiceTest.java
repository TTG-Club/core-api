package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.config.CacheConfig;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgRebuildStatus;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgRebuildStatus.State;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VttgCompendiumRebuildServiceTest {

    private final VttgChangesService changesService = mock(VttgChangesService.class);
    private final CacheManager cacheManager = mock(CacheManager.class);
    private final Cache fullExportCache = mock(Cache.class);
    /** Ручной исполнитель: задачи копятся и запускаются явно — видно, что запрос не ждёт пересборку. */
    private final Queue<Runnable> tasks = new ArrayDeque<>();
    private int executedTasks;
    private final VttgCompendiumRebuildService service =
            new VttgCompendiumRebuildService(changesService, cacheManager, tasks::add);

    VttgCompendiumRebuildServiceTest() {
        when(cacheManager.getCache(CacheConfig.VTTG_FULL_EXPORT)).thenReturn(fullExportCache);
    }

    @Test
    void initialStatusIsIdle() {
        assertEquals(VttgRebuildStatus.IDLE, service.status());
    }

    @Test
    void requestReturnsRunningAndRebuildsInBackground() {
        VttgRebuildStatus requested = service.request();

        assertEquals(State.RUNNING, requested.status());
        assertNotNull(requested.startedAt());
        verify(changesService, never()).changes(any(), any(), any(), anyBoolean());

        runTasks();

        verify(fullExportCache).clear();
        verify(changesService).changes(isNull(), isNull(), isNull(), eq(false));
        VttgRebuildStatus done = service.status();
        assertEquals(State.DONE, done.status());
        assertEquals(requested.startedAt(), done.startedAt());
        assertNotNull(done.finishedAt());
        assertNull(done.error());
    }

    @Test
    void requestDuringRunDoesNotStartSecondRunButRepeatsAfter() {
        service.request();
        AtomicInteger runs = new AtomicInteger();
        // Пока идёт первый прогон, приходит ещё один запрос.
        when(changesService.changes(any(), any(), any(), anyBoolean())).thenAnswer(invocation -> {
            if (runs.incrementAndGet() == 1) {
                service.request();
                assertEquals(State.RUNNING, service.status().status());
            }
            return null;
        });

        runTasks();

        assertEquals(1, executedTasks, "второй параллельный прогон не запускается");
        verify(changesService, times(2)).changes(isNull(), isNull(), isNull(), eq(false));
        assertEquals(State.DONE, service.status().status());
    }

    @Test
    void failureIsReportedToAdmin() {
        when(changesService.changes(any(), any(), any(), anyBoolean()))
                .thenThrow(new IllegalStateException("БД недоступна"));

        service.request();
        runTasks();

        VttgRebuildStatus failed = service.status();
        assertEquals(State.FAILED, failed.status());
        assertEquals("БД недоступна", failed.error());
        assertNotNull(failed.finishedAt());

        // После сбоя новый запрос снова запускает пересборку.
        service.request();
        assertEquals(1, tasks.size());
    }

    private void runTasks() {
        Runnable task;
        while ((task = tasks.poll()) != null) {
            executedTasks++;
            task.run();
        }
    }

}

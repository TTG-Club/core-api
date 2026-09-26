package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.config.CacheConfig;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgRebuildStatus;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgRebuildStatus.State;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Фоновая полная пересборка выгрузки VTTG после смены версии компендиума.
 *
 * <p>Пересборка — обычная полная выгрузка через Spring-прокси {@link VttgChangesService}: она
 * пересчитывает все строки {@code vttg_export} под новую версию и заодно прогревает кэш полного
 * дампа. Одновременно идёт не больше одной пересборки: запрос во время прогона не запускает
 * вторую, а ставит повтор сразу после текущей (версия могла смениться посреди прогона).</p>
 *
 * <p>Статус хранится в памяти экземпляра — после рестарта {@link VttgRebuildStatus#IDLE}.</p>
 */
@Service
public class VttgCompendiumRebuildService {

    private static final Logger log = LoggerFactory.getLogger(VttgCompendiumRebuildService.class);

    /** Предел длины текста ошибки для админа. */
    private static final int ERROR_MAX_LENGTH = 300;

    private final VttgChangesService changesService;
    private final CacheManager cacheManager;
    private final Executor executor;

    /** Идёт ли прогон (под {@code this}). */
    private boolean running;
    /** Нужен ли ещё один прогон после текущего (под {@code this}). */
    private boolean pending;
    private volatile VttgRebuildStatus status = VttgRebuildStatus.IDLE;

    @Autowired
    public VttgCompendiumRebuildService(VttgChangesService changesService, CacheManager cacheManager) {
        // Отдельный однопоточный пул: полная выгрузка сама раскладывает типы по пулу экспорта
        // и ждёт их — запуск её внутри того же пула занимал бы его поток впустую.
        this(changesService, cacheManager, Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "vttg-compendium-rebuild");
            thread.setDaemon(true);
            return thread;
        }));
    }

    VttgCompendiumRebuildService(VttgChangesService changesService, CacheManager cacheManager, Executor executor) {
        this.changesService = changesService;
        this.cacheManager = cacheManager;
        this.executor = executor;
    }

    /** Состояние последней пересборки. */
    public VttgRebuildStatus status() {
        return status;
    }

    /** Сбрасывает кэш полного дампа: до истечения TTL он отдавал бы данные старой версии. */
    public void evictFullExport() {
        Cache cache = cacheManager.getCache(CacheConfig.VTTG_FULL_EXPORT);
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * Запрашивает пересборку. Не блокирует: прогон идёт в фоне. Если пересборка уже идёт,
     * вторая параллельно не запускается — будет ещё один прогон сразу после текущего.
     *
     * @return состояние сразу после запроса ({@link State#RUNNING})
     */
    public synchronized VttgRebuildStatus request() {
        pending = true;
        if (!running) {
            running = true;
            status = new VttgRebuildStatus(State.RUNNING, Instant.now(), null, null);
            try {
                executor.execute(this::drain);
            } catch (RuntimeException rejected) {
                running = false;
                pending = false;
                status = failed(status.startedAt(), rejected);
                throw rejected;
            }
        }
        return status;
    }

    /** Прогоняет пересборки, пока есть запросы. Перед каждым прогоном статус уже RUNNING. */
    private void drain() {
        while (true) {
            Instant startedAt;
            synchronized (this) {
                if (!pending) {
                    running = false;
                    return;
                }
                pending = false;
                startedAt = status.startedAt();
            }
            VttgRebuildStatus result;
            try {
                evictFullExport();
                changesService.changes(null, null, null, false);
                result = new VttgRebuildStatus(State.DONE, startedAt, Instant.now(), null);
                log.info("VTTG: пересборка выгрузки завершена");
            } catch (Throwable failure) {
                log.error("VTTG: пересборка выгрузки упала", failure);
                result = failed(startedAt, failure);
            }
            synchronized (this) {
                // Ждёт следующий прогон — статус остаётся RUNNING до его конца.
                status = pending ? new VttgRebuildStatus(State.RUNNING, Instant.now(), null, null) : result;
            }
        }
    }

    private static VttgRebuildStatus failed(Instant startedAt, Throwable failure) {
        String message = failure.getMessage() != null ? failure.getMessage() : failure.getClass().getSimpleName();
        if (message.length() > ERROR_MAX_LENGTH) {
            message = message.substring(0, ERROR_MAX_LENGTH) + "…";
        }
        return new VttgRebuildStatus(State.FAILED, startedAt, Instant.now(), message);
    }

    @PreDestroy
    void shutdown() {
        if (executor instanceof ExecutorService service) {
            service.shutdownNow();
        }
    }
}

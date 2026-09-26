package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.vttg.model.VttgCompendiumVersion;
import club.ttg.dnd5.domain.vttg.repository.VttgCompendiumVersionRepository;
import club.ttg.dnd5.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Текущая версия формата выгрузки VTTG ({@code vttg_compendium_version}).
 *
 * <p>Значение держится в памяти и перечитывается из базы не чаще раза в {@link #REFRESH_INTERVAL_NANOS}:
 * версия нужна на каждой выгрузке, а при нескольких экземплярах core-api поднятие версии на одном
 * должно дойти до остальных за несколько секунд. Экземпляр, принявший запись, обновляет память сразу.
 * Версия только растёт, поэтому из двух значений (память и база) всегда берётся большее — запоздавшее
 * чтение не откатит свежую запись.</p>
 */
@Service
public class VttgCompendiumVersionService {

    private static final Logger log = LoggerFactory.getLogger(VttgCompendiumVersionService.class);

    /** Как долго значение в памяти считается актуальным без перечитывания из базы. */
    static final long REFRESH_INTERVAL_NANOS = 5_000_000_000L;

    private final VttgCompendiumVersionRepository repository;
    private final TransactionTemplate readTx;
    private final TransactionTemplate writeTx;

    private volatile Snapshot snapshot;

    public VttgCompendiumVersionService(VttgCompendiumVersionRepository repository,
                                        PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.readTx = new TransactionTemplate(transactionManager);
        this.readTx.setReadOnly(true);
        this.writeTx = new TransactionTemplate(transactionManager);
    }

    /** Текущая версия формата выгрузки. */
    public int current() {
        return get().version();
    }

    /** Текущая версия с отметкой, кто и когда её поднял. */
    public Snapshot get() {
        Snapshot cached = snapshot;
        if (cached != null && System.nanoTime() - cached.loadedAtNanos() < REFRESH_INTERVAL_NANOS) {
            return cached;
        }
        try {
            return remember(load());
        } catch (RuntimeException failure) {
            if (cached == null) {
                throw failure;
            }
            // Сбой базы не должен ронять выгрузку: работаем на последнем известном значении.
            log.warn("VTTG: не удалось перечитать версию компендиума, остаётся {}: {}",
                    cached.version(), failure.toString());
            return cached;
        }
    }

    /**
     * Поднимает версию. Проверка «строго больше текущей» выполняется атомарно в UPDATE.
     *
     * @throws ApiException 409, если новая версия не больше текущей
     */
    public Snapshot raise(int version, String username) {
        // База хранит микросекунды — усекаем сразу, чтобы ответ PUT совпадал с последующим GET.
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        Integer updated = writeTx.execute(status ->
                repository.raise(VttgCompendiumVersion.SINGLE_ROW_ID, version, now, username));
        if (updated == null || updated == 0) {
            Snapshot actual = remember(load());
            throw new ApiException(HttpStatus.CONFLICT,
                    "Версия должна быть больше текущей (" + actual.version() + ")");
        }
        log.info("VTTG: версия компендиума поднята до {} ({})", version, username);
        return remember(new Snapshot(version, now, username, System.nanoTime()));
    }

    private Snapshot load() {
        VttgCompendiumVersion row = readTx.execute(status ->
                repository.findById(VttgCompendiumVersion.SINGLE_ROW_ID)
                        .orElseThrow(() -> new IllegalStateException(
                                "Нет строки vttg_compendium_version с id = 1")));
        return new Snapshot(row.getVersion(), row.getUpdatedAt(), row.getUpdatedBy(), System.nanoTime());
    }

    private synchronized Snapshot remember(Snapshot fresh) {
        Snapshot cached = snapshot;
        if (cached != null && cached.version() > fresh.version()) {
            // Чтение стартовало до нашей записи — оставляем более новую версию, но продлеваем её.
            fresh = new Snapshot(cached.version(), cached.updatedAt(), cached.updatedBy(), fresh.loadedAtNanos());
        }
        snapshot = fresh;
        return fresh;
    }

    /**
     * Версия формата выгрузки.
     *
     * @param loadedAtNanos когда значение прочитано из базы или записано ({@link System#nanoTime()})
     */
    public record Snapshot(int version, Instant updatedAt, String updatedBy, long loadedAtNanos) {
    }
}

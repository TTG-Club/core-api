package club.ttg.dnd5.domain.common.url;

import club.ttg.dnd5.domain.source.event.SourceAcronymChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Прежние адреса записей каталога ({@code entity_url_history}) в памяти.
 *
 * <p>Нужны на каждом запросе страницы, а таблица маленькая и меняется только при смене
 * акронима источника, поэтому держится целиком и перечитывается не чаще раза в
 * {@link #REFRESH_INTERVAL_NANOS}; экземпляр, сменивший адреса, перечитывает сразу.</p>
 */
@Component
public class EntityUrlHistoryRegistry {
    private static final Logger log = LoggerFactory.getLogger(EntityUrlHistoryRegistry.class);
    static final long REFRESH_INTERVAL_NANOS = 5_000_000_000L;

    private final JdbcTemplate jdbc;
    /**
     * Чтение в отдельной транзакции: реестр опрашивают изнутри чужих (сохранение записи,
     * выгрузка), и сбой запроса — например, таблицы ещё нет — не должен их обрывать.
     */
    private final TransactionTemplate readTx;
    /** Раздел → прежний адрес → текущий. */
    private volatile Map<String, Map<String, String>> history = Map.of();
    private volatile long loadedAtNanos;
    private volatile boolean loaded;

    public EntityUrlHistoryRegistry(JdbcTemplate jdbc, PlatformTransactionManager transactionManager) {
        this.jdbc = jdbc;
        this.readTx = new TransactionTemplate(transactionManager);
        this.readTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.readTx.setReadOnly(true);
    }

    /**
     * Текущий адрес записи по прежнему.
     *
     * @param section раздел ({@link CatalogTables#SECTIONS})
     * @param oldUrl  адрес, по которому пришли
     * @return текущий адрес; пусто, если адрес не числится прежним
     */
    public Optional<String> currentUrl(String section, String oldUrl) {
        Map<String, String> urls = current().get(section);
        return urls == null ? Optional.empty() : Optional.ofNullable(urls.get(oldUrl));
    }

    /** Числится ли адрес прежним у какой-либо записи раздела. */
    public boolean isOldUrl(String section, String url) {
        return currentUrl(section, url).isPresent();
    }

    private Map<String, Map<String, String>> current() {
        if (!loaded || System.nanoTime() - loadedAtNanos > REFRESH_INTERVAL_NANOS) {
            try {
                refresh();
            } catch (RuntimeException failure) {
                if (!loaded) {
                    // Пустая карта до следующей попытки через интервал — без запроса на каждый вызов.
                    log.warn("Не удалось прочитать прежние адреса записей: {}", failure.toString());
                    loaded = true;
                    loadedAtNanos = System.nanoTime();
                    return Map.of();
                }
                log.warn("Не удалось перечитать прежние адреса записей: {}", failure.toString());
                loadedAtNanos = System.nanoTime();
            }
        }
        return history;
    }

    /** Перечитывает таблицу. */
    public synchronized void refresh() {
        Map<String, Map<String, String>> fresh = new HashMap<>();
        readTx.executeWithoutResult(status -> jdbc.query("select section, old_url, new_url from entity_url_history",
                row -> {
                    fresh.computeIfAbsent(row.getString(1), key -> new HashMap<>())
                            .put(row.getString(2), row.getString(3));
                }));
        Map<String, Map<String, String>> frozen = new HashMap<>();
        fresh.forEach((section, urls) -> frozen.put(section, Map.copyOf(urls)));
        history = Map.copyOf(frozen);
        loadedAtNanos = System.nanoTime();
        loaded = true;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAcronymChanged(SourceAcronymChangedEvent event) {
        refresh();
    }
}

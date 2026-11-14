package club.ttg.dnd5.domain.vttg.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Держит в памяти карту {@code vttg_url_alias} для {@link VttgUrlAliases}.
 *
 * <p>Карта маленькая (только записи со сменённым адресом) и нужна на каждой выгрузке,
 * поэтому перечитывается из базы не чаще раза в {@link #REFRESH_INTERVAL_NANOS}: смена
 * адреса на одном экземпляре core-api доходит до остальных за несколько секунд. Экземпляр,
 * сменивший адрес, перечитывает её сразу ({@link #refresh()}).</p>
 */
@Component
public class VttgUrlAliasRegistry {
    private static final Logger log = LoggerFactory.getLogger(VttgUrlAliasRegistry.class);
    static final long REFRESH_INTERVAL_NANOS = 5_000_000_000L;

    private final JdbcTemplate jdbc;
    /**
     * Чтение в отдельной транзакции: реестр опрашивают изнутри чужих (сохранение записи,
     * выгрузка), и сбой запроса — например, таблицы ещё нет — не должен их обрывать.
     */
    private final TransactionTemplate readTx;
    private volatile Map<String, String> aliases = Map.of();
    private volatile long loadedAtNanos;
    private volatile boolean loaded;

    public VttgUrlAliasRegistry(JdbcTemplate jdbc, PlatformTransactionManager transactionManager) {
        this.jdbc = jdbc;
        this.readTx = new TransactionTemplate(transactionManager);
        this.readTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.readTx.setReadOnly(true);
    }

    @PostConstruct
    void install() {
        VttgUrlAliases.install(this::current);
    }

    @PreDestroy
    void uninstall() {
        VttgUrlAliases.reset();
    }

    /** Текущая карта «адрес на сайте → первоначальный адрес». */
    public Map<String, String> current() {
        if (!loaded || System.nanoTime() - loadedAtNanos > REFRESH_INTERVAL_NANOS) {
            try {
                refresh();
            } catch (RuntimeException failure) {
                if (!loaded) {
                    // Пустая карта до следующей попытки через интервал — без запроса на каждый вызов.
                    log.warn("VTTG: не удалось прочитать прежние адреса записей: {}", failure.toString());
                    loaded = true;
                    loadedAtNanos = System.nanoTime();
                    return Map.of();
                }
                // Сбой базы не должен ронять выгрузку: работаем на последней известной карте.
                log.warn("VTTG: не удалось перечитать прежние адреса записей: {}", failure.toString());
                loadedAtNanos = System.nanoTime();
            }
        }
        return aliases;
    }

    /** Перечитывает карту из базы. */
    public synchronized void refresh() {
        Map<String, String> fresh = new HashMap<>();
        readTx.executeWithoutResult(status -> jdbc.query("select url, original_url from vttg_url_alias",
                row -> {
                    fresh.put(row.getString(1), row.getString(2));
                }));
        aliases = Map.copyOf(fresh);
        loadedAtNanos = System.nanoTime();
        loaded = true;
    }
}

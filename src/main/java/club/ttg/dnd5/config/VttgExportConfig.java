package club.ttg.dnd5.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Инфраструктура параллельной сборки экспорта VTTG.
 *
 * <p>Полная выгрузка {@code /changes} тяжела из-за гидрации jsonb-сущностей и сетевых задержек
 * до (удалённой) БД. Типы обрабатываются параллельно, каждый — в своей read-only транзакции.
 * Пул ограничен ({@code vttg.export.parallelism}, по умолчанию 6 &lt; размера пула Hikari),
 * чтобы параллельные задачи не исчерпали соединения БД.</p>
 */
@Configuration
public class VttgExportConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService vttgExportExecutor(
            @Value("${vttg.export.parallelism:6}") int parallelism) {
        return Executors.newFixedThreadPool(parallelism, runnable -> {
            Thread thread = new Thread(runnable, "vttg-export");
            thread.setDaemon(true);
            return thread;
        });
    }
}

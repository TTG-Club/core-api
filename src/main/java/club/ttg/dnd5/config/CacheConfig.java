package club.ttg.dnd5.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig  {

    /** Имя кэша полного экспорта VTTG (см. {@code VttgChangesService}). */
    public static final String VTTG_FULL_EXPORT = "vttgFullExport";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(100));
        // Полный экспорт VTTG (/changes без since) тяжёлый (~9с, гидрация jsonb-сущностей) и
        // детерминирован в пределах короткого окна. Короткий TTL делает повторные выгрузки
        // мгновенными без массовой инвалидации; инкрементальный поллинг (since задан) не кэшируется.
        cacheManager.registerCustomCache(VTTG_FULL_EXPORT,
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(32)
                        .build());
        return cacheManager;
    }
}

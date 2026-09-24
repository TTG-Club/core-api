package club.ttg.dnd5.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Настройки клиента find-game-api (каталог игр): куда ходить за составом игры и с
 * какими конечными таймаутами. Спрашиваем его, кто мастер и кто игрок, — бастионы
 * игроков строятся поверх игр каталога.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "find-game-service")
public class FindGameServiceProperties {
    private String baseUrl;
    private Duration connectTimeout = Duration.ofSeconds(2);
    private Duration readTimeout = Duration.ofSeconds(3);
    /** Сколько помнить ответ о составе игры: права проверяются на каждом запросе. */
    private Duration membershipCacheTtl = Duration.ofMinutes(1);
}

package club.ttg.dnd5.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Настройки клиента subscriber-service: куда ходить за статусом подписки и с какими
 * конечными таймаутами (быстро отваливаемся в fail-closed при недоступности).
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "subscriber-service")
public class SubscriberServiceProperties {
    private String baseUrl;
    private Duration connectTimeout = Duration.ofSeconds(2);
    private Duration readTimeout = Duration.ofSeconds(3);
}

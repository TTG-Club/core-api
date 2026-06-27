package club.ttg.dnd5.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Общий секрет для аутентификации межсервисных вызовов (внутренние ручки
 * {@code /api/internal/**} и исходящие вызовы subscriber-service).
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "internal")
public class InternalServiceProperties {
    private String serviceSecret;
}

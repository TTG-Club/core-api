package club.ttg.dnd5.config.properties;

import java.time.Duration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.ratelimit")
public class RateLimitProperties
{
    private long capacity = 200;
    private Duration window = Duration.ofMinutes(1);
    private Duration bucketIdleTtl = Duration.ofMinutes(30);
}
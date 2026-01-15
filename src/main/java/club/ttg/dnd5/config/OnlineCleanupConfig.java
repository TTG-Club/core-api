package club.ttg.dnd5.config;

import club.ttg.dnd5.domain.common.service.OnlineUserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.Instant;

@EnableScheduling
@Configuration
public class OnlineCleanupConfig
{
    private final OnlineUserService service;

    public OnlineCleanupConfig(OnlineUserService service)
    {
        this.service = service;
    }

    @Scheduled(fixedRate = 15 * 60_000L)
    public void cleanup()
    {
        service.cleanup(Duration.ofMinutes(30), Instant.now());
    }
}

package club.ttg.dnd5.domain.tool.tracker.service;

import club.ttg.dnd5.config.properties.TrackerProperties;
import club.ttg.dnd5.exception.ApiException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Ограничение создания анонимных трекеров инициативы по IP (bucket4j, в памяти).
 * <p>
 * Лимит «один трекер анониму» держит клиент (localStorage); сервер этой защитой
 * лишь пресекает массовое создание мусорных трекеров с одного адреса.
 */
@RequiredArgsConstructor
@Component
public class TrackerCreationRateLimiter {

    private final TrackerProperties properties;

    private final Map<String, BucketEntry> buckets = new ConcurrentHashMap<>();

    public void checkAnonymousCreation(String clientIp) {
        BucketEntry entry = buckets.computeIfAbsent(clientIp,
                ip -> new BucketEntry(newBucket(), new AtomicReference<>(Instant.now())));
        entry.lastSeen().set(Instant.now());
        if (!entry.bucket().tryConsume(1)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Слишком много анонимных трекеров. Попробуйте позже или войдите в аккаунт");
        }
    }

    /**
     * IP клиента с учётом X-Forwarded-For — тот же способ, что в
     * {@link club.ttg.dnd5.config.RateLimitFilter}.
     */
    public static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(properties.getAnonymousCreateLimit())
                .refillGreedy(properties.getAnonymousCreateLimit(), properties.getAnonymousCreateWindow())
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /** Очистка простаивающих bucket'ов, чтобы карта не росла бесконечно. */
    @Scheduled(fixedDelayString = "PT30M")
    public void cleanup() {
        Instant cutoff = Instant.now().minus(properties.getBucketIdleTtl());
        buckets.entrySet().removeIf(entry -> entry.getValue().lastSeen().get().isBefore(cutoff));
    }

    private record BucketEntry(Bucket bucket, AtomicReference<Instant> lastSeen) {
    }
}

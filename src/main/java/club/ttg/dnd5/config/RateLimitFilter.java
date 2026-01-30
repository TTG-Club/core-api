package club.ttg.dnd5.config;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import club.ttg.dnd5.config.properties.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter
{
    private final RateLimitProperties properties;

    private final Map<String, BucketEntry> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitProperties properties)
    {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException
    {
        String clientKey = resolveClientKey(request);

        BucketEntry entry = buckets.computeIfAbsent(clientKey, k -> new BucketEntry(newBucket()));
        entry.touch();

        ConsumptionProbe probe = entry.bucket().tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed())
        {
            response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
            return;
        }

        long waitNanos = probe.getNanosToWaitForRefill();
        long retryAfterSeconds = Math.max(1, Duration.ofNanos(waitNanos).toSeconds());

        response.setStatus(429);
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.getWriter().write("Too many requests");
    }

    private Bucket newBucket()
    {
        Bandwidth limit = Bandwidth.builder()
                .capacity(properties.getCapacity())
                .refillGreedy(properties.getCapacity(), properties.getWindow())
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Ключ лимита:
     * - если пользователь авторизован: user:<username>
     * - иначе: ip:<ip> (с учётом X-Forwarded-For)
     */
    private String resolveClientKey(HttpServletRequest request)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null)
        {
            return "user:" + authentication.getName();
        }

        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank())
        {
            String ip = forwarded.split(",")[0].trim();
            return "ip:" + ip;
        }

        return "ip:" + request.getRemoteAddr();
    }

    /**
     * Очистка idle bucket’ов, чтобы карта не росла бесконечно.
     */
    @Scheduled(fixedDelayString = "PT5M")
    public void cleanup()
    {
        Instant cutoff = Instant.now().minus(properties.getBucketIdleTtl());
        buckets.entrySet().removeIf(e -> e.getValue().lastSeen().isBefore(cutoff));
    }

    private record BucketEntry(Bucket bucket, LastSeenHolder holder)
    {
        BucketEntry(Bucket bucket)
        {
            this(bucket, new LastSeenHolder());
        }

        void touch()
        {
            holder.touch();
        }

        Instant lastSeen()
        {
            return holder.lastSeen();
        }
    }

    private static final class LastSeenHolder
    {
        private volatile Instant lastSeen = Instant.now();

        void touch()
        {
            lastSeen = Instant.now();
        }

        Instant lastSeen()
        {
            return lastSeen;
        }
    }
}
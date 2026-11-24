package club.ttg.dnd5.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;
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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter
{
    private static final String ONLINE_HEARTBEAT_PATH = "/api/v2/online/heartbeat";
    /** Минутные отметки работы с инструментом: иначе они съедали бы общий лимит запросов гостя. */
    private static final String ONLINE_USAGE_PATH = "/api/v2/online/usage";
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    /** Только цифры, точки, двоеточия и hex: имя хоста сюда не пройдёт и DNS не спросит. */
    private static final Pattern IP_LITERAL = Pattern.compile("[0-9a-fA-F.:]+");

    private final RateLimitProperties properties;

    private final Map<String, BucketEntry> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitProperties properties)
    {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(@NotNull HttpServletRequest request)
    {
        String path = request.getServletPath();
        if (path == null || path.isEmpty()) {
            path = request.getRequestURI();
        }
        return ONLINE_HEARTBEAT_PATH.equals(path) || ONLINE_USAGE_PATH.equals(path)
                || isInternalServiceRequest(request);
    }

    /**
     * Запрос соседнего сервиса по внутренней сети, а не посетителя.
     *
     * <p>Снаружи запросы приходят через обратный прокси, и он всегда проставляет
     * {@code X-Forwarded-For}. Без этого заголовка и с адресом частной сети приходит только
     * свой сервис — прежде всего сайт, когда собирает страницу на сервере: у таких запросов
     * нет адреса посетителя. Раньше все они считались по адресу контейнера сайта в одном
     * ведре на всех посетителей и ботов сразу, и при его исчерпании страницы падали с 429.</p>
     *
     * @param request входящий запрос.
     * @return {@code true}, если запрос пришёл напрямую из частной сети.
     */
    static boolean isInternalServiceRequest(HttpServletRequest request)
    {
        String forwarded = request.getHeader(FORWARDED_FOR_HEADER);
        if (forwarded != null && !forwarded.isBlank())
        {
            return false;
        }

        return isPrivateAddress(request.getRemoteAddr());
    }

    /** Адрес частной сети или локальный: 10/8, 172.16/12, 192.168/16, 127/8, ::1, fc00::/7. */
    private static boolean isPrivateAddress(String address)
    {
        if (address == null || address.isBlank() || !IP_LITERAL.matcher(address).matches())
        {
            return false;
        }

        try
        {
            // Литерал адреса разбирается без обращения к DNS
            InetAddress parsed = InetAddress.getByName(address);
            byte[] bytes = parsed.getAddress();
            boolean uniqueLocalV6 = bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc;

            return parsed.isSiteLocalAddress() || parsed.isLoopbackAddress() || uniqueLocalV6;
        }
        catch (UnknownHostException exception)
        {
            return false;
        }
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
     * <p>
     * Анонимный токен Spring Security тоже отвечает {@code isAuthenticated() == true}
     * с именем {@code anonymousUser} — без его исключения все незалогиненные
     * клиенты делили бы один бакет.
     */
    private String resolveClientKey(HttpServletRequest request)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.getName() != null)
        {
            return "user:" + authentication.getName();
        }

        String forwarded = request.getHeader(FORWARDED_FOR_HEADER);
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

package club.ttg.dnd5.domain.common.service;

import club.ttg.dnd5.domain.common.model.OnlineType;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class OnlineUserService
{
    private final ConcurrentMap<String, Instant> guestLastSeen = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Instant> registeredLastSeen = new ConcurrentHashMap<>();

    public void heartbeat(OnlineType type, String key, Instant now)
    {
        if (type == OnlineType.REGISTERED)
        {
            registeredLastSeen.put(key, now);
        }
        else
        {
            guestLastSeen.put(key, now);
        }
    }

    public OnlineCount getCount(Duration window, Instant now)
    {
        long guests = countFresh(guestLastSeen, window, now);
        long registered = countFresh(registeredLastSeen, window, now);
        return new OnlineCount(guests, registered, guests + registered);
    }

    public void cleanup(Duration window, Instant now)
    {
        evictStale(guestLastSeen, window, now);
        evictStale(registeredLastSeen, window, now);
    }

    private static long countFresh(Map<String, Instant> map, Duration window, Instant now)
    {
        return map.values().stream()
                .filter(ts -> Duration.between(ts, now).compareTo(window) <= 0)
                .count();
    }

    private static void evictStale(ConcurrentMap<String, Instant> map, Duration window, Instant now)
    {
        map.entrySet().removeIf(e -> Duration.between(e.getValue(), now).compareTo(window) > 0);
    }

    public record OnlineCount(long guests, long registered, long total) {}
}


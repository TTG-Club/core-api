package club.ttg.dnd5.domain.filter.rest;

import club.ttg.dnd5.dto.base.filters.QueryFilter;
import club.ttg.dnd5.dto.base.filters.QuerySingleton;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Парсер GET-параметров в типизированные {@link QueryFilter} и {@link QuerySingleton}.
 * <p>
 * Формат URL-параметров:
 * <ul>
 *   <li>{@code key=a,b,c} — значения через запятую</li>
 *   <li>{@code key_mode=1} — режим exclude</li>
 *   <li>{@code key_union=1} — логика OR</li>
 *   <li>{@code key=1} / {@code key=0} — для singleton</li>
 * </ul>
 */
@UtilityClass
public class QueryParamFilterResolver
{
    private static final String MODE_SUFFIX = "_mode";
    private static final String UNION_SUFFIX = "_union";
    private static final String FLAG_ON = "1";

    /**
     * Парсит enum-фильтр: {@code type=beast,dragon} → {@code QueryFilter<CreatureType>}.
     */
    public <E extends Enum<E>> QueryFilter<E> resolveEnum(final Map<String, String[]> params,
                                                           final String key,
                                                           final Class<E> enumClass)
    {
        QueryFilter<E> filter = new QueryFilter<>();
        String raw = getFirst(params, key);

        if (raw == null || raw.isBlank())
        {
            return filter;
        }

        Set<E> values = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> parseEnum(s, enumClass))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        filter.setValues(values);
        filter.setExclude(isFlag(params, key + MODE_SUFFIX));
        filter.setUnion(isFlag(params, key + UNION_SUFFIX));

        return filter;
    }

    /**
     * Парсит числовой фильтр: {@code cr=1,2,3} → {@code QueryFilter<Long>}.
     */
    public QueryFilter<Long> resolveLong(final Map<String, String[]> params,
                                          final String key)
    {
        QueryFilter<Long> filter = new QueryFilter<>();
        String raw = getFirst(params, key);

        if (raw == null || raw.isBlank())
        {
            return filter;
        }

        Set<Long> values = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s ->
                {
                    try
                    {
                        return Long.parseLong(s);
                    }
                    catch (NumberFormatException e)
                    {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        filter.setValues(values);
        filter.setExclude(isFlag(params, key + MODE_SUFFIX));
        filter.setUnion(isFlag(params, key + UNION_SUFFIX));

        return filter;
    }

    /**
     * Парсит строковый фильтр: {@code tag=f4a2b1c9,1a2b3c4d} → {@code QueryFilter<String>}.
     * Значения берутся как есть (SHA-256 хэши или акронимы).
     */
    public QueryFilter<String> resolveString(final Map<String, String[]> params,
                                              final String key)
    {
        QueryFilter<String> filter = new QueryFilter<>();
        String raw = getFirst(params, key);

        if (raw == null || raw.isBlank())
        {
            return filter;
        }

        Set<String> values = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        filter.setValues(values);
        filter.setExclude(isFlag(params, key + MODE_SUFFIX));
        filter.setUnion(isFlag(params, key + UNION_SUFFIX));

        return filter;
    }

    /**
     * Парсит singleton: {@code lair=1} → positive, {@code lair=0} → negative.
     */
    public QuerySingleton resolveSingleton(final Map<String, String[]> params,
                                            final String key)
    {
        QuerySingleton singleton = new QuerySingleton();
        String raw = getFirst(params, key);

        if (raw == null || raw.isBlank())
        {
            return singleton;
        }

        if (FLAG_ON.equals(raw.trim()))
        {
            singleton.setValue(true);
        }
        else if ("0".equals(raw.trim()))
        {
            singleton.setValue(false);
        }

        return singleton;
    }

    /**
     * Парсит множество источников: {@code source=DMG,MM,TCE} → {@code Set<String>}.
     * Без _mode — всегда include.
     */
    public Set<String> resolveSources(final Map<String, String[]> params,
                                       final String key)
    {
        String raw = getFirst(params, key);

        if (raw == null || raw.isBlank())
        {
            return Set.of();
        }

        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private String getFirst(final Map<String, String[]> params, final String key)
    {
        String[] arr = params.get(key);
        return (arr != null && arr.length > 0) ? arr[0] : null;
    }

    private boolean isFlag(final Map<String, String[]> params, final String key)
    {
        return FLAG_ON.equals(getFirst(params, key));
    }

    private <E extends Enum<E>> E parseEnum(final String value, final Class<E> enumClass)
    {
        try
        {
            return Enum.valueOf(enumClass, value.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }
}

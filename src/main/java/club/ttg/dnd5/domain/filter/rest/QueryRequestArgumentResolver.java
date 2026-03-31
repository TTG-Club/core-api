package club.ttg.dnd5.domain.filter.rest;

import club.ttg.dnd5.dto.base.filters.AbstractQueryRequest;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring {@link HandlerMethodArgumentResolver} для автоматического парсинга
 * подклассов {@link AbstractQueryRequest} из GET-параметров.
 * <p>
 * Парсинг выполняется за один проход по query string через токенизацию:
 * строка разбивается на токены {@code key=value}, из которых собирается Map.
 * Затем аннотированные {@link FilterParam} поля заполняются на основе этой Map.
 */
public class QueryRequestArgumentResolver implements HandlerMethodArgumentResolver
{
    private static final String MODE_SUFFIX = "_mode";
    private static final String UNION_SUFFIX = "_union";
    private static final String FLAG_ON = "1";

    @Override
    public boolean supportsParameter(MethodParameter parameter)
    {
        return AbstractQueryRequest.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                   ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest,
                                   WebDataBinderFactory binderFactory) throws Exception
    {
        HttpServletRequest httpRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Class<?> clazz = parameter.getParameterType();
        AbstractQueryRequest request = (AbstractQueryRequest) clazz.getDeclaredConstructor().newInstance();

        // 1. Токенизация: один проход по query string → Map<String, String>
        Map<String, String> tokens = tokenize(httpRequest != null ? httpRequest.getQueryString() : null);

        // 2. Базовые поля из AbstractQueryRequest
        String searchVal = tokens.get("search");
        if (searchVal != null)
        {
            request.setSearch(searchVal);
        }

        String pageVal = tokens.get("page");
        if (pageVal != null)
        {
            try { request.setPage(Integer.parseInt(pageVal)); }
            catch (NumberFormatException ignored) { /* дефолт */ }
        }

        String sizeVal = tokens.get("size");
        if (sizeVal != null)
        {
            try { request.setPageSize(Integer.parseInt(sizeVal)); }
            catch (NumberFormatException ignored) { /* дефолт */ }
        }

        String sourceVal = tokens.get("source");
        if (sourceVal != null)
        {
            request.setSource(splitToSet(sourceVal));
        }

        // 3. Парсинг аннотированных полей из токенов
        for (Field field : collectAnnotatedFields(clazz))
        {
            FilterParam ann = field.getAnnotation(FilterParam.class);
            String key = ann.value().isEmpty() ? field.getName() : ann.value();

            String raw = tokens.get(key);
            if (raw == null) continue;

            field.setAccessible(true);
            boolean modeFlag = FLAG_ON.equals(tokens.get(key + MODE_SUFFIX));
            boolean unionFlag = FLAG_ON.equals(tokens.get(key + UNION_SUFFIX));

            if (QueryFilter.class.isAssignableFrom(field.getType()))
            {
                QueryFilter<?> filter = buildQueryFilter(raw, modeFlag, unionFlag, field, ann);
                field.set(request, filter);
            }
        }

        return request;
    }

    /**
     * Один проход по query string: разбивает на пары key=value.
     * Результат — Map (первое значение для каждого ключа).
     */
    Map<String, String> tokenize(String queryString)
    {
        if (queryString == null || queryString.isEmpty())
        {
            return Map.of();
        }

        Map<String, String> result = new HashMap<>();
        int len = queryString.length();
        int pos = 0;

        while (pos < len)
        {
            int ampIdx = queryString.indexOf('&', pos);
            if (ampIdx == -1) ampIdx = len;

            int eqIdx = queryString.indexOf('=', pos);
            if (eqIdx == -1 || eqIdx > ampIdx)
            {
                pos = ampIdx + 1;
                continue;
            }

            String key = URLDecoder.decode(queryString.substring(pos, eqIdx), StandardCharsets.UTF_8);
            String val = URLDecoder.decode(queryString.substring(eqIdx + 1, ampIdx), StandardCharsets.UTF_8);

            result.putIfAbsent(key, val);
            pos = ampIdx + 1;
        }

        return result;
    }

    /**
     * CSV → Set.
     */
    Set<String> splitToSet(String raw)
    {
        if (raw == null || raw.isBlank())
        {
            return Set.of();
        }

        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Строит {@link QueryFilter} нужного типа на основе рефлексии generic-параметра поля.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private QueryFilter<?> buildQueryFilter(String raw, boolean mode, boolean union,
                                              Field field, FilterParam ann)
    {
        QueryFilter filter = new QueryFilter<>();
        Set<String> rawValues = splitToSet(raw);

        if (rawValues.isEmpty())
        {
            return filter;
        }

        Class<?> genericType = resolveGenericType(field);

        if (genericType != null && genericType.isEnum() && ann.enumClass() != Enum.class)
        {
            Set enumValues = rawValues.stream()
                    .map(s -> parseEnum(s, (Class<Enum>) ann.enumClass()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            filter.setValues(enumValues);
        }
        else if (genericType == Long.class)
        {
            Set<Long> longValues = rawValues.stream()
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
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            filter.setValues(longValues);
        }
        else
        {
            filter.setValues(rawValues);
        }

        filter.setExclude(mode);
        filter.setUnion(union);
        return filter;
    }

    /**
     * Собирает все поля с {@link FilterParam} из класса и его суперклассов.
     */
    List<Field> collectAnnotatedFields(Class<?> clazz)
    {
        List<Field> result = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class)
        {
            for (Field field : current.getDeclaredFields())
            {
                if (field.isAnnotationPresent(FilterParam.class))
                {
                    result.add(field);
                }
            }
            current = current.getSuperclass();
        }

        return result;
    }

    /**
     * Извлекает generic-тип поля {@code QueryFilter<T>}.
     */
    private Class<?> resolveGenericType(Field field)
    {
        Type genericType = field.getGenericType();

        if (genericType instanceof ParameterizedType pt)
        {
            Type[] typeArgs = pt.getActualTypeArguments();

            if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?>)
            {
                return (Class<?>) typeArgs[0];
            }
        }

        return null;
    }

    private <E extends Enum<E>> E parseEnum(String value, Class<E> enumClass)
    {
        if (value == null || value.isBlank())
        {
            return null;
        }

        for (E constant : enumClass.getEnumConstants())
        {
            if (constant.name().equalsIgnoreCase(value))
            {
                return constant;
            }
        }

        return null;
    }
}

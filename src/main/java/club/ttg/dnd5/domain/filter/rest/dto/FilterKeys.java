package club.ttg.dnd5.domain.filter.rest.dto;

import club.ttg.dnd5.domain.filter.rest.FilterParam;
import club.ttg.dnd5.dto.base.filters.AbstractQueryRequest;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class FilterKeys
{
    /**
     * Возвращает имя GET-параметра для данного поля QueryRequest.
     * Если не найдено или нет @FilterParam, возвращает само fieldName.
     */
    public String keyOf(Class<? extends AbstractQueryRequest> clazz, String fieldName)
    {
        try
        {
            Field field = clazz.getDeclaredField(fieldName);
            FilterParam ann = field.getAnnotation(FilterParam.class);
            if (ann != null && !ann.value().isEmpty())
            {
                return ann.value();
            }
        }
        catch (NoSuchFieldException ignored)
        {
        }
        return fieldName;
    }

    /**
     * Возвращает все ключи, определенные в классе через @FilterParam.
     */
    public List<String> allKeys(Class<? extends AbstractQueryRequest> clazz)
    {
        List<String> keys = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class)
        {
            for (Field field : current.getDeclaredFields())
            {
                if (field.isAnnotationPresent(FilterParam.class))
                {
                    FilterParam ann = field.getAnnotation(FilterParam.class);
                    keys.add(ann.value().isEmpty() ? field.getName() : ann.value());
                }
            }
            current = current.getSuperclass();
        }

        return keys;
    }
}

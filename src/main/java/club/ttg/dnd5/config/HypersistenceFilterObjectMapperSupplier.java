package club.ttg.dnd5.config;

import club.ttg.dnd5.dto.base.filters.FilterRegistry;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import io.hypersistence.utils.hibernate.type.util.ObjectMapperSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.LinkedHashMap;
import java.util.Map;

public class HypersistenceFilterObjectMapperSupplier implements ObjectMapperSupplier
{
    private static final String BASE_PACKAGE = "club.ttg.dnd5";

    @Override
    public ObjectMapper get()
    {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(FilterRegistry.class));

        Map<String, Class<?>> seenTypeIds = new LinkedHashMap<>();

        for (BeanDefinition bd : scanner.findCandidateComponents(BASE_PACKAGE))
        {
            Class<?> root = loadClass(bd.getBeanClassName());

            registerIfHasTypeName(mapper, seenTypeIds, root);

            for (Class<?> nested : root.getDeclaredClasses())
            {
                JsonTypeName tn = nested.getAnnotation(JsonTypeName.class);
                if (tn != null && !tn.value().isBlank())
                {
                    registerIfHasTypeName(mapper, seenTypeIds, nested);
                }
            }
        }

        return mapper;
    }

    private static void registerIfHasTypeName(
            ObjectMapper mapper,
            Map<String, Class<?>> seenTypeIds,
            Class<?> clazz)
    {
        JsonTypeName typeName = clazz.getAnnotation(JsonTypeName.class);
        if (typeName == null || typeName.value().isBlank())
        {
            return;
        }

        String id = typeName.value();
        Class<?> previous = seenTypeIds.putIfAbsent(id, clazz);

        if (previous != null && !previous.equals(clazz))
        {
            throw new IllegalStateException(
                    "Duplicate @JsonTypeName('" + id + "') for classes: "
                            + previous.getName() + " and " + clazz.getName()
            );
        }

        mapper.registerSubtypes(new NamedType(clazz, id));
    }

    private static Class<?> loadClass(String className)
    {
        try
        {
            return Class.forName(className);
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException("Cannot load class: " + className, e);
        }
    }
}
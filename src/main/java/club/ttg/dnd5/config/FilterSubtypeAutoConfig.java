package club.ttg.dnd5.config;

import club.ttg.dnd5.dto.base.filters.FilterRegistry;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class FilterSubtypeAutoConfig
{
    private static final String BASE_PACKAGE = "club.ttg.dnd5.domain";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer autoRegisterFilterSubtypes()
    {
        return builder -> builder.postConfigurer(this::registerAllFilterSubtypes);
    }

    private void registerAllFilterSubtypes(ObjectMapper objectMapper)
    {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(FilterRegistry.class));

        // Чтобы ловить дубликаты id заранее и получать нормальную ошибку, а не случайное поведение.
        Map<String, Class<?>> seenTypeIds = new LinkedHashMap<>();

        for (BeanDefinition bd : scanner.findCandidateComponents(BASE_PACKAGE))
        {
            Class<?> root = loadClass(bd.getBeanClassName());

            registerIfHasTypeName(objectMapper, seenTypeIds, root);

            for (Class<?> nested : root.getDeclaredClasses())
            {
                if (nested.isAnnotationPresent(FilterRegistry.class))
                {
                    registerIfHasTypeName(objectMapper, seenTypeIds, nested);
                }
                else
                {
                    JsonTypeName tn = nested.getAnnotation(JsonTypeName.class);
                    if (tn != null && !tn.value().isBlank())
                    {
                        registerIfHasTypeName(objectMapper, seenTypeIds, nested);
                    }
                }
            }
        }
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

    private static Class<?> loadClass(String name)
    {
        try
        {
            return Class.forName(name);
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException("Cannot load class: " + name, e);
        }
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder)
    {
        ObjectMapper mapper = builder.build();
        mapper.deactivateDefaultTyping();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}

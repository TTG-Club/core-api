package club.ttg.dnd5.config;

import club.ttg.dnd5.domain.filter.rest.FilterParam;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Configuration
public class OpenApiConfig
{
    private static final String MODE_SUFFIX = "_mode";
    private static final String UNION_SUFFIX = "_union";

    @Bean
    public OpenAPI openAPI()
    {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createApiKeyScheme()))
                .info(createApiInfo())
                .addServersItem(new Server().url("/").description("Базовый URL сервера"));
    }

    @Bean
    public OperationCustomizer queryFilterOperationCustomizer()
    {
        return (operation, handlerMethod) ->
        {
            if (operation == null)
            {
                return null;
            }

            List<Parameter> parameters = operation.getParameters() == null
                    ? new ArrayList<>()
                    : new ArrayList<>(operation.getParameters());

            for (MethodParameter methodParameter : handlerMethod.getMethodParameters())
            {
                if (!isParameterObject(methodParameter))
                {
                    continue;
                }

                processParameterObject(parameters, methodParameter.getParameterType());
            }

            operation.setParameters(parameters);
            return operation;
        };
    }

    private void processParameterObject(List<Parameter> parameters, Class<?> parameterType)
    {
        for (Field field : getAllFields(parameterType))
        {
            if (!QueryFilter.class.isAssignableFrom(field.getType()))
            {
                continue;
            }

            String filterName = resolveFilterName(field);
            FilterParam filterParam = field.getAnnotation(FilterParam.class);

            parameters.removeIf(parameter -> isBrokenFilterParameter(parameter, filterName));

            parameters.add(buildMainFilterParameter(field, filterName, filterParam));
            parameters.add(buildModeParameter(filterName));
            parameters.add(buildUnionParameter(filterName));
        }
    }

    private boolean isParameterObject(MethodParameter methodParameter)
    {
        for (Annotation annotation : methodParameter.getParameterAnnotations())
        {
            if (annotation.annotationType() == ParameterObject.class)
            {
                return true;
            }
        }

        return false;
    }

    private String resolveFilterName(Field field)
    {
        FilterParam filterParam = field.getAnnotation(FilterParam.class);

        if (filterParam != null && filterParam.value() != null && !filterParam.value().isBlank())
        {
            return filterParam.value();
        }

        return field.getName();
    }

    private Parameter buildMainFilterParameter(Field field, String filterName, FilterParam filterParam)
    {
        ArraySchema schema = new ArraySchema();
        schema.setItems(resolveItemSchema(field, filterParam));

        Parameter parameter = new Parameter();
        parameter.setName(filterName);
        parameter.setIn("query");
        parameter.setRequired(false);
        parameter.setDescription(buildValuesDescription(filterName, filterParam));
        parameter.setSchema(schema);
        parameter.setStyle(Parameter.StyleEnum.FORM);
        parameter.setExplode(false);

        Object example = resolveExampleValue(filterParam);
        if (example != null)
        {
            parameter.setExample(example);
        }

        return parameter;
    }

    private Parameter buildModeParameter(String filterName)
    {
        Schema<String> schema = new Schema<>();
        schema.setType("string");
        schema.setEnum(List.of("1"));

        Parameter parameter = new Parameter();
        parameter.setName(filterName + MODE_SUFFIX);
        parameter.setIn("query");
        parameter.setRequired(false);
        parameter.setDescription("Режим исключения. Укажите 1, чтобы исключить выбранные значения.");
        parameter.setSchema(schema);
        parameter.setExample("1");

        return parameter;
    }

    private Parameter buildUnionParameter(String filterName)
    {
        Schema<String> schema = new Schema<>();
        schema.setType("string");
        schema.setEnum(List.of("1"));

        Parameter parameter = new Parameter();
        parameter.setName(filterName + UNION_SUFFIX);
        parameter.setIn("query");
        parameter.setRequired(false);
        parameter.setDescription("Режим объединения (ИЛИ). Укажите 1, чтобы искать по любому из значений.");
        parameter.setSchema(schema);
        parameter.setExample("1");

        return parameter;
    }

    private Schema<?> resolveItemSchema(Field field, FilterParam filterParam)
    {
        Class<?> enumClass = filterParam != null ? filterParam.enumClass() : Enum.class;

        if (enumClass != null && enumClass != Enum.class && enumClass.isEnum())
        {
            Schema<String> schema = new Schema<>();
            schema.setType("string");
            schema.setEnum(Arrays.stream(enumClass.getEnumConstants())
                    .map(Object::toString)
                    .toList());
            return schema;
        }

        Class<?> genericType = resolveQueryFilterGenericType(field);
        if (genericType != null && genericType.isEnum())
        {
            Schema<String> schema = new Schema<>();
            schema.setType("string");
            schema.setEnum(Arrays.stream(genericType.getEnumConstants())
                    .map(Object::toString)
                    .toList());
            return schema;
        }

        Schema<String> schema = new Schema<>();
        schema.setType("string");
        return schema;
    }

    private Object resolveExampleValue(FilterParam filterParam)
    {
        Class<?> enumClass = filterParam != null ? filterParam.enumClass() : Enum.class;

        if (enumClass != null && enumClass != Enum.class && enumClass.isEnum())
        {
            Object[] constants = enumClass.getEnumConstants();
            if (constants.length > 0)
            {
                return constants[0].toString();
            }
        }

        return null;
    }

    private Class<?> resolveQueryFilterGenericType(Field field)
    {
        Type genericType = field.getGenericType();

        if (genericType instanceof ParameterizedType pt)
        {
            Type[] args = pt.getActualTypeArguments();
            if (args.length > 0 && args[0] instanceof Class<?> clazz)
            {
                return clazz;
            }
        }

        return null;
    }

    private String buildValuesDescription(String filterName, FilterParam filterParam)
    {
        String base = "Значения фильтра '" + filterName + "'. Можно передать одно значение или список через запятую.";

        if (filterParam != null && filterParam.description() != null && !filterParam.description().isBlank())
        {
            return filterParam.description() + ". Можно передать одно значение или список через запятую.";
        }

        return base;
    }

    private boolean isBrokenFilterParameter(Parameter parameter, String filterName)
    {
        if (parameter == null || parameter.getName() == null)
        {
            return false;
        }

        String name = parameter.getName();
        return Objects.equals(name, filterName)
                || Objects.equals(name, filterName + ".values")
                || Objects.equals(name, filterName + ".exclude")
                || Objects.equals(name, filterName + ".union")
                || Objects.equals(name, filterName + MODE_SUFFIX)
                || Objects.equals(name, filterName + UNION_SUFFIX);
    }

    private List<Field> getAllFields(Class<?> type)
    {
        List<Field> fields = new ArrayList<>();
        Class<?> current = type;

        while (current != null && current != Object.class)
        {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }

        return fields;
    }

    private SecurityScheme createApiKeyScheme()
    {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
    }

    private Info createApiInfo()
    {
        return new Info()
                .title("TTG REST API")
                .version("2.0")
                .description("Документация API проекта TTG.")
                .contact(new Contact()
                        .name("Команда поддержки")
                        .email("support@ttg.club")
                        .url("https://ttg.club"));
    }
}
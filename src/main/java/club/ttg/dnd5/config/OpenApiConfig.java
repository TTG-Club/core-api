package club.ttg.dnd5.config;

import club.ttg.dnd5.domain.filter.rest.FilterParam;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
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
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Configuration
public class OpenApiConfig
{
    @Bean
    public OpenAPI openAPI()
    {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createApiKeyScheme()))
                .info(createApiInfo())
                .addServersItem(new Server().url("/").description("Default Server URL"));
    }

    @Bean
    public OperationCustomizer queryFilterOperationCustomizer()
    {
        return (operation, handlerMethod) -> {
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

            parameters.add(buildValuesParameter(field, filterName, filterParam));
            parameters.add(buildExcludeParameter(filterName));
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

    private Parameter buildValuesParameter(Field field, String filterName, FilterParam filterParam)
    {
        ArraySchema schema = new ArraySchema();
        schema.setItems(resolveItemSchema(field, filterParam));

        Parameter parameter = new Parameter();
        parameter.setName(filterName + ".values");
        parameter.setIn("query");
        parameter.setRequired(false);
        parameter.setDescription(buildValuesDescription(filterName, filterParam));
        parameter.setSchema(schema);
        parameter.setExplode(true);

        return parameter;
    }

    private Parameter buildExcludeParameter(String filterName)
    {
        Parameter parameter = new Parameter();
        parameter.setName(filterName + ".exclude");
        parameter.setIn("query");
        parameter.setRequired(false);
        parameter.setDescription("Exclude mode for filter " + filterName);
        parameter.setSchema(new BooleanSchema()._default(false));

        return parameter;
    }

    private Parameter buildUnionParameter(String filterName)
    {
        Parameter parameter = new Parameter();
        parameter.setName(filterName + ".union");
        parameter.setIn("query");
        parameter.setRequired(false);
        parameter.setDescription("OR mode for filter " + filterName);
        parameter.setSchema(new BooleanSchema()._default(false));

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

    private Class<?> resolveQueryFilterGenericType(Field field)
    {
        Type genericType = field.getGenericType();

        if (!(genericType instanceof ParameterizedType parameterizedType))
        {
            return null;
        }

        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length == 0)
        {
            return null;
        }

        Type firstArgument = actualTypeArguments[0];
        if (firstArgument instanceof Class<?> clazz)
        {
            return clazz;
        }

        return null;
    }

    private String buildValuesDescription(String filterName, FilterParam filterParam)
    {
        if (filterParam != null && filterParam.enumClass() != null && filterParam.enumClass() != Enum.class)
        {
            return "Values of filter " + filterName + " (" + filterParam.enumClass().getSimpleName() + ")";
        }

        return "Values of filter " + filterName;
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
                || Objects.equals(name, filterName + ".union");
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
                .description("This is the API documentation for the TTG application.")
                .contact(new Contact()
                        .name("Support Team")
                        .email("support@ttg.club")
                        .url("https://ttg.club"));
    }
}
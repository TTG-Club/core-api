package club.ttg.dnd5.domain.filter.rest.dto;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Типы групп фильтров в метаданных.
 * Используется в {@link FilterMetadataResponse.FilterGroupMeta}.
 */
public enum FilterGroupType
{
    FILTER("filter"),
    SINGLETON("singleton");

    private final String value;

    FilterGroupType(String value)
    {
        this.value = value;
    }

    @JsonValue
    public String getValue()
    {
        return value;
    }
}

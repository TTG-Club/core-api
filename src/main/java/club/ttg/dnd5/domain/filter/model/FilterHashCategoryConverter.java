package club.ttg.dnd5.domain.filter.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FilterHashCategoryConverter implements AttributeConverter<FilterHashCategory, String> {

    @Override
    public String convertToDatabaseColumn(FilterHashCategory attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public FilterHashCategory convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        try {
            return FilterHashCategory.valueOf(dbData.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            // Разрешаем принудительное чтение, даже если Enum не найден (вернув null или подходящий default), 
            // но в нашем случае достаточно toUpperCase() для исправления старой базы.
            throw new IllegalArgumentException("Unknown filter hash category in DB: " + dbData, exception);
        }
    }
}

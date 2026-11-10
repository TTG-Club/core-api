package club.ttg.dnd5.dto.base.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Как {@link FormattedMarkupDescriptionSerializer}, но пустое значение отдаёт пустой строкой, а не
 * {@code null}: форма для правки уходит обратно в POST/PUT как есть и не падает на проверке «не пусто».
 */
public class FormattedMarkupOrEmptySerializer extends FormattedMarkupDescriptionSerializer {

    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        if (StringUtils.isEmpty(value)) {
            jsonGenerator.writeString("");
            return;
        }
        super.serialize(value, jsonGenerator, serializerProvider);
    }
}

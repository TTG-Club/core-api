package club.ttg.dnd5.dto.base.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Как {@link MarkupDescriptionSerializer}, но пустое значение (в том числе {@code null} — подключать
 * и через {@code nullsUsing}) отдаёт пустой строкой: для необязательного текста, который фронт
 * всё равно ждёт разметкой (строка, массив или объект), а не {@code null}.
 */
public class MarkupDescriptionOrEmptySerializer extends MarkupDescriptionSerializer {

    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        if (StringUtils.isBlank(value)) {
            jsonGenerator.writeString("");
            return;
        }
        super.serialize(value, jsonGenerator, serializerProvider);
    }
}

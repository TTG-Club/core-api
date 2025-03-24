package club.ttg.dnd5.dto.base.serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class MarkupDescriptionSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String s, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if(StringUtils.isNotEmpty(s)){
            jsonGenerator.writeRawValue(s);
        } else {
            jsonGenerator.writeNull();
        }
    }
}

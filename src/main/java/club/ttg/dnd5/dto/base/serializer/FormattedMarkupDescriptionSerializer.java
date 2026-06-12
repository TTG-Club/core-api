package club.ttg.dnd5.dto.base.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class FormattedMarkupDescriptionSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        if (StringUtils.isEmpty(value)) {
            jsonGenerator.writeNull();
            return;
        }

        ObjectMapper mapper = (ObjectMapper) jsonGenerator.getCodec();
        try {
            JsonNode node = mapper.readTree(value);
            if (node.isArray() || node.isObject()) {
                jsonGenerator.writeString(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node));
                return;
            }
        } catch (IOException ignored) {
            // Non-JSON markup is still a valid editor value.
        }

        jsonGenerator.writeString(value);
    }
}

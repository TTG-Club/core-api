package club.ttg.dnd5.dto.base.deserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class MarkupDescriptionDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode node = mapper.readTree(jsonParser);

        // Если поле — строка, возможно в ней закодирован JSON
        if (node.isTextual()) {
            String raw = node.textValue();
            try {
                JsonNode parsed = mapper.readTree(raw);
                // Только если это массив или объект — сериализуем
                if (parsed.isArray() || parsed.isObject()) {
                    return mapper.writeValueAsString(parsed);
                }
            } catch (IOException ignored) {
                raw = raw.replace("\n\n", "\",\"");
                raw = "[\"" + raw + "\"]";
                JsonNode parsed = mapper.readTree(raw);
                // Только если это массив или объект — сериализуем
                if (parsed.isArray() || parsed.isObject()) {
                    return mapper.writeValueAsString(parsed);
                }
            }
            return raw;
        }
        // Если сразу JSON (объект или массив)
        return mapper.writeValueAsString(node);
    }
}

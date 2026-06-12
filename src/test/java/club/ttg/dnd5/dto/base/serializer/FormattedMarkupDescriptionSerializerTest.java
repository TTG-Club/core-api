package club.ttg.dnd5.dto.base.serializer;

import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormattedMarkupDescriptionSerializerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesMarkupJsonAsFormattedString() throws Exception {
        FeatRequest request = new FeatRequest();
        request.setDescription("{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\"}]}");

        String json = objectMapper.writeValueAsString(request);
        String description = objectMapper.readTree(json).get("description").textValue();

        assertTrue(description.contains("\n"));
        assertTrue(description.contains("\"type\" : \"doc\""));
        assertTrue(description.contains("\"content\" : [ {"));
    }

    @Test
    void keepsNonJsonMarkupAsString() throws Exception {
        FeatRequest request = new FeatRequest();
        request.setDescription("plain text");

        String json = objectMapper.writeValueAsString(request);

        assertEquals("plain text", objectMapper.readTree(json).get("description").textValue());
    }
}

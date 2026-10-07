package club.ttg.dnd5.domain.vttg.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VttgAutomationStripperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VttgAutomationStripper stripper = new VttgAutomationStripper(objectMapper);

    @Test
    void removesEffectsOnEveryLevelOfNonSrdRecord() throws Exception {
        JsonNode payload = objectMapper.readTree("""
                {"id":"artificer","isSRD":false,"hitDie":8,
                 "activeEffects":[{"id":"a"}],
                 "features":[{"key":"f","activeEffects":[{"id":"b"}],
                              "choices":[{"key":"c","activeEffects":[{"id":"c"}],"featData":{"modifiers":[1]}}]}],
                 "subclasses":[{"key":"s","activeEffects":[{"id":"d"}]}]}
                """);

        JsonNode stripped = (JsonNode) stripper.strip(payload);

        assertFalse(stripped.toString().contains("activeEffects"));
        assertFalse(stripped.get("automation").asBoolean());
        // Остальная механика остаётся.
        assertEquals(8, stripped.get("hitDie").asInt());
        assertTrue(stripped.at("/features/0/choices/0/featData/modifiers").isArray());
        // Исходное дерево общее на всех (кэш payload) — оно не меняется.
        assertTrue(payload.has("activeEffects"));
        assertFalse(payload.has("automation"));
    }

    /** Эффекты действий существа лежат глубоко в статблоке — вырезаются и они, урон остаётся. */
    @Test
    void removesEffectsOfCreatureActions() throws Exception {
        JsonNode payload = objectMapper.readTree("""
                {"id":"beast","isSRD":false,
                 "system":{"actions":[{"name":"Укус","attackBonus":5,
                                       "damageParts":[{"formula":"1d8"}],
                                       "activeEffects":[{"id":"poisoned"}]}]},
                 "spells":[{"id":"hex","isSRD":true,"activeEffects":[{"id":"hex"}]}]}
                """);

        JsonNode stripped = (JsonNode) stripper.strip(payload);

        assertFalse(stripped.toString().contains("activeEffects"));
        assertEquals(5, stripped.at("/system/actions/0/attackBonus").asInt());
        assertTrue(stripped.at("/system/actions/0/damageParts").isArray());
        assertFalse(stripped.get("automation").asBoolean());
    }

    @Test
    void keepsSrdRecordUntouched() throws Exception {
        JsonNode payload = objectMapper.readTree("""
                {"id":"fireball","isSRD":true,"activeEffects":[{"id":"a"}]}
                """);

        assertSame(payload, stripper.strip(payload));
    }

    @Test
    void doesNotMarkNonSrdRecordWithoutEffects() throws Exception {
        JsonNode payload = objectMapper.readTree("""
                {"id":"spell","isSRD":false,"damageParts":[{"formula":"1d6"}]}
                """);

        assertSame(payload, stripper.strip(payload));
    }

    @Test
    void keepsFeatSeparatorMap() {
        Map<String, Object> separator = Map.of("id", "origin", "type", "separator");

        assertSame(separator, stripper.strip(separator));
    }

    @Test
    void stripsMapperDto() {
        JsonNode stripped = (JsonNode) stripper.strip(new Dto("feat", false, new String[]{"effect"}));

        assertFalse(stripped.has("activeEffects"));
        assertFalse(stripped.get("automation").asBoolean());
        assertEquals("feat", stripped.get("id").asText());
    }

    private record Dto(String id, @com.fasterxml.jackson.annotation.JsonProperty("isSRD") boolean srd,
                       String[] activeEffects) {
    }
}

package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.glossary.model.Glossary;
import club.ttg.dnd5.domain.source.model.Source;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VttgGlossaryMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VttgGlossaryMapper mapper = new VttgGlossaryMapper(new VttgMarkupConverter(objectMapper));

    /** «Захват» — постоянные поля типа, категория тега и ключ источника. */
    @Test
    void mapsEntryToVttgFormat() {
        Glossary glossary = baseGlossary("grapple-phb", "Захват", "Grapple");
        glossary.setTagCategory("действие");
        glossary.setDescription("Цель должна быть не более чем на один размер больше вас.");

        JsonNode json = json(glossary);
        assertEquals("grapple-phb", json.get("id").asText());
        assertEquals("Захват", json.get("name").asText());
        assertEquals("Grapple", json.get("nameEn").asText());
        assertEquals("glossary", json.get("type").asText());
        assertEquals("glossary", json.get("section").asText());
        assertEquals("Действие", json.get("category").asText());
        assertEquals("Глоссарий", json.get("typeLabel").asText());
        assertFalse(json.has("source"));
        assertEquals("phb", json.get("sourceKey").asText());
        assertTrue(json.get("isSRD").asBoolean());
        assertTrue(json.get("description").asText().contains("на один размер больше"));
    }

    /**
     * Идентичность страницы-источника: по ней VTTG находит термин в компендиуме, когда в описании
     * заклинания или черты кликают ссылку {@code {@glossary ...}}.
     */
    @Test
    void exportsSourcePageIdentity() {
        JsonNode json = json(baseGlossary("prone-phb", "Сбитый с ног", "Prone"));

        assertEquals("glossary", json.get("srcSection").asText());
        assertEquals("prone-phb", json.get("srcUrl").asText());
    }

    /** Записи без тега получают категорию по умолчанию — иначе группировка сложила бы их в безымянную группу. */
    @Test
    void fallsBackToDefaultCategory() {
        Glossary glossary = baseGlossary("inspiration", "Вдохновение", "Inspiration");
        glossary.setTagCategory(null);

        assertEquals("Прочее", json(glossary).get("category").asText());
    }

    /** Не входящая в SRD запись — {@code isSRD = false}. */
    @Test
    void marksNonSrdEntry() {
        Glossary glossary = baseGlossary("bastion", "Бастион", "Bastion");
        glossary.setSrdVersion(null);

        assertFalse(json(glossary).get("isSRD").asBoolean());
    }

    private JsonNode json(Glossary glossary) {
        return objectMapper.valueToTree(mapper.toVttg(glossary));
    }

    private Glossary baseGlossary(String url, String name, String english) {
        Glossary glossary = new Glossary();
        glossary.setUrl(url);
        glossary.setName(name);
        glossary.setEnglish(english);
        glossary.setDescription("");
        Source source = new Source();
        source.setAcronym("PHB24");
        source.setName("PHB 2024");
        glossary.setSource(source);
        glossary.setSrdVersion("5.1");
        return glossary;
    }
}

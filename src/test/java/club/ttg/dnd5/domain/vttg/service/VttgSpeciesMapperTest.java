package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.model.SpeciesFeature;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesSizeDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VttgSpeciesMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VttgSpeciesMapper mapper = new VttgSpeciesMapper(new VttgMarkupConverter(objectMapper));

    /** «Драконорождённый» — тип/размер/скорость, тёмное зрение в grants, умения в features. */
    @Test
    void mapsDragonbornToVttgFormat() {
        Species species = baseSpecies("dragonborn", "Драконорожденный", "Dragonborn");
        species.setType(CreatureType.DRAGON);
        species.setSizes(List.of(size(Size.MEDIUM)));
        species.setSpeed(30);
        species.setDarkVision(60);
        species.setFeatures(List.of(
                new SpeciesFeature("draconic-flight", "Драконий Полёт", "Draconic Flight", "Призрачные крылья.", null)));

        JsonNode json = json(species);
        assertEquals("species", json.get("type").asText());
        assertEquals("species", json.get("section").asText());
        assertEquals("dragonborn", json.get("key").asText());
        // id обязателен для раскладки дельты (routeEntity: <id>.json) и равен key; isSRD → SRD-пак.
        assertEquals("dragonborn", json.get("id").asText());
        assertTrue(json.get("isSRD").asBoolean());
        assertEquals("Dragonborn", json.get("nameEn").asText());
        assertFalse(json.has("source"));
        assertEquals("phb", json.get("sourceKey").asText());
        assertEquals("dragon", json.get("creatureType").asText());
        assertEquals("[\"medium\"]", json.get("size").toString());
        assertEquals(30, json.get("speed").get("walk").asInt());
        assertFalse(json.get("speed").has("fly"));

        assertEquals(1, json.get("grants").size());
        assertEquals("darkvision", json.get("grants").get(0).get("type").asText());
        assertEquals(60, json.get("grants").get(0).get("range").asInt());

        assertEquals(1, json.get("features").size());
        JsonNode feature = json.get("features").get(0);
        assertEquals("draconic-flight", feature.get("key").asText());
        assertEquals("Драконий Полёт", feature.get("name").asText());
        assertTrue(feature.get("description").asText().contains("крылья"));
    }

    /** «Человек» — несколько размеров в порядке источника; вид без тёмного зрения даёт пустой grants. */
    @Test
    void mapsMultipleSizesAndEmptyGrants() {
        Species species = baseSpecies("human", "Человек", "Human");
        species.setType(CreatureType.HUMANOID);
        species.setSizes(List.of(size(Size.SMALL), size(Size.MEDIUM)));
        species.setSpeed(30);

        JsonNode json = json(species);
        assertEquals("[\"small\",\"medium\"]", json.get("size").toString());
        assertTrue(json.get("grants").isArray());
        assertEquals(0, json.get("grants").size());
    }

    /** «Эльф» — происхождения (дочерние виды) сворачиваются в choices умения-происхождения. */
    @Test
    void embedsLineagesAsChoicesOnLineageFeature() {
        Species elf = baseSpecies("elf", "Эльф", "Elf");
        elf.setType(CreatureType.HUMANOID);
        elf.setSizes(List.of(size(Size.MEDIUM)));
        elf.setSpeed(30);
        elf.setFeatures(List.of(
                new SpeciesFeature("trance", "Транс", "Trance", "Не нужно спать.", null),
                new SpeciesFeature("elf-lineage", "Происхождения эльфов", "Elf Lineage", "Выберите происхождение.", null)));

        Species high = baseSpecies("high-elf", "Высший эльф", "High Elf");
        high.setDescription("Магия высших эльфов.");
        high.setFeatures(List.of(new SpeciesFeature("high-magic", "Магия", "Magic", "Престидижитация.", null)));
        Species drow = baseSpecies("drow", "Дроу", "Drow");
        drow.setDescription("Дроу из Подземья.");
        elf.setLineages(List.of(high, drow));

        JsonNode json = json(elf);
        assertEquals(2, json.get("features").size());
        assertFalse(json.get("features").get(0).has("choices"));

        JsonNode lineage = json.get("features").get(1);
        assertEquals("elf-lineage", lineage.get("key").asText());
        JsonNode choices = lineage.get("choices");
        // Сортировка по имени (кириллица): «Высший эльф» < «Дроу».
        assertEquals(2, choices.size());
        assertEquals("high-elf", choices.get(0).get("key").asText());
        assertEquals("Высший эльф", choices.get(0).get("name").asText());
        assertTrue(choices.get(0).get("description").asText().contains("Престидижитация"));
        assertEquals("drow", choices.get(1).get("key").asText());
    }

    private JsonNode json(Species species) {
        return objectMapper.valueToTree(mapper.toVttg(species));
    }

    private Species baseSpecies(String url, String name, String english) {
        Species species = new Species();
        species.setUrl(url);
        species.setName(name);
        species.setEnglish(english);
        species.setDescription("");
        Source source = new Source();
        source.setAcronym("PHB24");
        source.setName("PHB 2024");
        species.setSource(source);
        return species;
    }

    private SpeciesSizeDto size(Size type) {
        SpeciesSizeDto dto = new SpeciesSizeDto();
        dto.setType(type);
        return dto;
    }
}

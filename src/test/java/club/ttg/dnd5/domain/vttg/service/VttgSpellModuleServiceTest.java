package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgSpell;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgSpellScaling;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VttgSpellModuleServiceTest {
    private final SpellRepository repository = mock(SpellRepository.class);
    private final VttgSpellMapper mapper = mock(VttgSpellMapper.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VttgSpellModuleService service = new VttgSpellModuleService(repository, mapper, objectMapper);

    @Test
    void buildsInstallableModuleArchiveForDefaultSrdVersion() throws Exception {
        String moduleId = "ttg-club-srd-spells";
        Spell source = new Spell();
        VttgSpell spell = VttgSpell.builder()
                .id("test-spell")
                .name("Тестовое заклинание")
                .level(1)
                .school("evocation")
                .castingTimeValue(1)
                .castingTimeUnit("action")
                .range(30)
                .rangeUnit("ft")
                .durationValue(0)
                .durationUnit("instantaneous")
                .targetType("creature")
                .targetCount(3)
                .deliveryType("ranged")
                .autoHit(true)
                .saveType("none")
                .saveEffect("half")
                .scaling(VttgSpellScaling.builder()
                        .additionalDice("1к6")
                        .additionalTargets(1)
                        .description("Усиление")
                        .build())
                .description("Описание")
                .isSRD(true)
                .type("spell")
                .build();
        when(repository.findAllVisibleForVttgExport(null)).thenReturn(List.of(source));
        when(mapper.toVttg(source)).thenReturn(spell);

        VttgModuleArchive archive = service.buildModule();
        Map<String, byte[]> entries = unzip(archive.content());

        assertEquals(moduleId + ".zip", archive.fileName());
        assertTrue(entries.containsKey(moduleId + "/module.json"));
        assertTrue(entries.containsKey(moduleId + "/client.js"));
        assertTrue(entries.containsKey(moduleId + "/spells.json"));

        JsonNode manifest = objectMapper.readTree(entries.get(moduleId + "/module.json"));
        assertEquals(moduleId, manifest.get("id").asText());
        assertEquals("client.js", manifest.at("/client/entry").asText());
        assertEquals("client.js", manifest.at("/scripts/0").asText());

        JsonNode spells = objectMapper.readTree(entries.get(moduleId + "/spells.json"));
        assertEquals(1, spells.size());
        assertEquals("test-spell", spells.get(0).get("id").asText());
        assertEquals("spell", spells.get(0).get("type").asText());
        assertEquals(3, spells.get(0).get("targetCount").asInt());
        assertTrue(spells.get(0).get("autoHit").asBoolean());
        assertEquals("half", spells.get(0).get("saveEffect").asText());
        assertEquals("1к6", spells.get(0).at("/scaling/additionalDice").asText());
        assertEquals(1, spells.get(0).at("/scaling/additionalTargets").asInt());
        assertEquals("Усиление", spells.get(0).at("/scaling/description").asText());
        assertEquals(spell.getDescription(), spells.get(0).get("description").asText());
        assertTrue(spells.get(0).get("isSRD").asBoolean());
        assertNull(spells.get(0).get("srd"));

        String client = new String(entries.get(moduleId + "/client.js"), StandardCharsets.UTF_8);
        assertTrue(client.contains("/module-assets/" + moduleId + "/spells.json"));
        verify(repository).findAllVisibleForVttgExport(null);
    }

    @Test
    void rejectsEmptySrdModule() {
        when(repository.findAllVisibleForVttgExport(null)).thenReturn(List.of());

        assertThrows(club.ttg.dnd5.exception.ContentNotFoundException.class, service::buildModule);
    }

    private Map<String, byte[]> unzip(byte[] archive) throws Exception {
        Map<String, byte[]> entries = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive), StandardCharsets.UTF_8)) {
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                entries.put(entry.getName(), zip.readAllBytes());
            }
        }
        return entries;
    }
}

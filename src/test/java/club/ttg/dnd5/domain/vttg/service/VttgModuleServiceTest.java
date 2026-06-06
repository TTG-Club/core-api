package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.repository.CreatureRepository;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgCreature;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgSpell;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VttgModuleServiceTest {
    private final SpellRepository spellRepository = mock(SpellRepository.class);
    private final CreatureRepository creatureRepository = mock(CreatureRepository.class);
    private final VttgSpellMapper spellMapper = mock(VttgSpellMapper.class);
    private final VttgCreatureMapper creatureMapper = mock(VttgCreatureMapper.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VttgModuleService service = new VttgModuleService(
            spellRepository, creatureRepository, spellMapper, creatureMapper, objectMapper);

    @Test
    void buildsCombinedAndSeparateModules() throws Exception {
        Spell spell = new Spell();
        Creature creature = new Creature();
        when(spellRepository.findAllVisibleBySrdVersion(VttgModuleService.DEFAULT_SRD_VERSION))
                .thenReturn(List.of(spell));
        when(creatureRepository.findAllVisibleBySrdVersion(VttgModuleService.DEFAULT_SRD_VERSION))
                .thenReturn(List.of(creature));
        when(spellMapper.toVttg(spell)).thenReturn(VttgSpell.builder().id("spell").build());
        when(creatureMapper.toVttg(creature)).thenReturn(VttgCreature.builder().id("creature").build());

        Map<String, byte[]> all = unzip(service.buildAllModule().content());
        Map<String, byte[]> spells = unzip(service.buildSpellModule().content());
        Map<String, byte[]> creatures = unzip(service.buildCreatureModule().content());

        assertTrue(hasFile(all, "spells.json"));
        assertTrue(hasFile(all, "creatures.json"));
        assertTrue(hasFile(spells, "spells.json"));
        assertFalse(hasFile(spells, "creatures.json"));
        assertTrue(hasFile(creatures, "creatures.json"));
        assertFalse(hasFile(creatures, "spells.json"));
    }

    @Test
    void groupsCreaturesByChallengeRatingAndSortsByExperienceThenName() throws Exception {
        Creature rat = creature("rat", "Крыса", 10);
        Creature zombie = creature("zombie", "Зомби", 200);
        Creature ape = creature("ape", "Обезьяна", 200);
        when(creatureRepository.findAllVisibleBySrdVersion(VttgModuleService.DEFAULT_SRD_VERSION))
                .thenReturn(List.of(zombie, rat, ape));
        when(creatureMapper.toVttg(rat)).thenReturn(VttgCreature.builder().id("rat").name("Крыса").build());
        when(creatureMapper.toVttg(zombie)).thenReturn(VttgCreature.builder().id("zombie").name("Зомби").build());
        when(creatureMapper.toVttg(ape)).thenReturn(VttgCreature.builder().id("ape").name("Обезьяна").build());

        Map<String, byte[]> files = unzip(service.buildCreatureModule().content());
        JsonNode creatures = objectMapper.readTree(creaturesJson(files));

        assertEquals("separator", creatures.get(0).get("type").asText());
        assertEquals("ПО 0", creatures.get(0).get("name").asText());
        assertEquals("rat", creatures.get(1).get("id").asText());
        assertEquals("ПО 1", creatures.get(2).get("name").asText());
        assertEquals("zombie", creatures.get(3).get("id").asText());
        assertEquals("ape", creatures.get(4).get("id").asText());
    }

    private boolean hasFile(Map<String, byte[]> files, String name) {
        return files.keySet().stream().anyMatch(path -> path.endsWith("/" + name));
    }

    private byte[] creaturesJson(Map<String, byte[]> files) {
        return files.entrySet().stream()
                .filter(entry -> entry.getKey().endsWith("/creatures.json"))
                .findFirst()
                .orElseThrow()
                .getValue();
    }

    private Creature creature(String url, String name, long experience) {
        Creature creature = new Creature();
        creature.setUrl(url);
        creature.setName(name);
        creature.setExperience(experience);
        return creature;
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

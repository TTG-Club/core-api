package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.repository.CreatureRepository;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.repository.MagicItemRepository;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgCreature;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgMagicItem;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VttgModuleServiceTest {
    private final SpellRepository spellRepository = mock(SpellRepository.class);
    private final CreatureRepository creatureRepository = mock(CreatureRepository.class);
    private final MagicItemRepository magicItemRepository = mock(MagicItemRepository.class);
    private final VttgSpellMapper spellMapper = mock(VttgSpellMapper.class);
    private final VttgCreatureMapper creatureMapper = mock(VttgCreatureMapper.class);
    private final VttgMagicItemMapper magicItemMapper = mock(VttgMagicItemMapper.class);
    private final VttgCompendiumSections sections = new VttgCompendiumSections();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VttgModuleService service = new VttgModuleService(
            spellRepository, creatureRepository, magicItemRepository,
            spellMapper, creatureMapper, magicItemMapper, sections, objectMapper);

    @Test
    void buildsPerSectionCompendiumLayoutWithoutClientJsOrSeparators() throws Exception {
        Spell spell = new Spell();
        spell.setUrl("fire-burst");
        spell.setLevel(3L);
        Creature creature = new Creature();
        creature.setUrl("goblin");
        creature.setExperience(100L); // ПО 1/2 → подпапка cr/1-2
        when(spellRepository.findAllVisibleForVttgExport(null)).thenReturn(List.of(spell));
        when(creatureRepository.findAllVisibleForVttgExport(null)).thenReturn(List.of(creature));
        when(magicItemRepository.findAllVisibleForVttgExport(null)).thenReturn(List.of());
        when(spellMapper.toVttg(spell)).thenReturn(VttgSpell.builder().id("fire-burst").build());
        when(creatureMapper.toVttg(creature)).thenReturn(VttgCreature.builder().id("goblin").build());

        Map<String, byte[]> all = unzip(service.buildAllModule().content());

        // Новый формат: compendium/ с тонким manifest.json, section.json и одним JSON на сущность.
        assertTrue(hasFile(all, "/compendium/manifest.json"));
        assertTrue(hasFile(all, "/compendium/spells/section.json"));
        assertTrue(hasFile(all, "/compendium/spells/3/fire-burst.json"));
        assertTrue(hasFile(all, "/compendium/creatures/section.json"));
        assertTrue(hasFile(all, "/compendium/creatures/cr/1-2/goblin.json"));

        // Легаси-артефактов нет.
        assertFalse(all.keySet().stream().anyMatch(path -> path.endsWith("client.js")));
        assertFalse(all.keySet().stream().anyMatch(path -> path.endsWith("/spells.json")));
        assertFalse(all.keySet().stream().anyMatch(path -> path.endsWith("/creatures.json")));

        // module.json без client/scripts.
        JsonNode module = readJson(all, "/module.json");
        assertTrue(module.at("/client").isMissingNode());
        assertTrue(module.at("/scripts").isMissingNode());

        // Тонкий manifest.json: sections[] и readOnly.
        JsonNode manifest = readJson(all, "/compendium/manifest.json");
        assertTrue(manifest.get("readOnly").asBoolean());
        List sectionIds = objectMapper.convertValue(manifest.get("sections"), List.class);
        assertTrue(sectionIds.contains("spells"));
        assertTrue(sectionIds.contains("creatures"));

        // section.json несёт канонический dataKind и view.
        JsonNode spellsSection = readJson(all, "/compendium/spells/section.json");
        assertEquals("spell", spellsSection.get("dataKind").asText());
        assertEquals("filtered", spellsSection.at("/view/layout").asText());
        JsonNode creaturesSection = readJson(all, "/compendium/creatures/section.json");
        assertEquals("creature", creaturesSection.get("dataKind").asText());
    }

    @Test
    void buildsSeparateSpellAndCreatureModules() throws Exception {
        Spell spell = new Spell();
        spell.setUrl("magic-missile");
        spell.setLevel(1L);
        Creature creature = new Creature();
        creature.setUrl("rat");
        creature.setExperience(10L);
        when(spellRepository.findAllVisibleForVttgExport(null)).thenReturn(List.of(spell));
        when(creatureRepository.findAllVisibleForVttgExport(null)).thenReturn(List.of(creature));
        when(spellMapper.toVttg(spell)).thenReturn(VttgSpell.builder().id("magic-missile").build());
        when(creatureMapper.toVttg(creature)).thenReturn(VttgCreature.builder().id("rat").build());

        Map<String, byte[]> spells = unzip(service.buildSpellModule().content());
        Map<String, byte[]> creatures = unzip(service.buildCreatureModule().content());

        assertTrue(hasFile(spells, "/compendium/spells/1/magic-missile.json"));
        assertFalse(spells.keySet().stream().anyMatch(path -> path.contains("/creatures/")));

        assertTrue(hasFile(creatures, "/compendium/creatures/cr/0/rat.json"));
        assertFalse(creatures.keySet().stream().anyMatch(path -> path.contains("/spells/")));
    }

    @Test
    void buildsMagicItemModuleAsEquipmentSection() throws Exception {
        MagicItem item = new MagicItem();
        when(magicItemRepository.findAllVisibleForVttgExport(null)).thenReturn(List.of(item));
        when(magicItemMapper.toVttg(eq(item), any())).thenReturn(VttgMagicItem.builder().id("srd_wand_of_fear").build());

        Map<String, byte[]> files = unzip(service.buildMagicItemModule().content());

        assertTrue(hasFile(files, "/compendium/magic-items/section.json"));
        assertTrue(hasFile(files, "/compendium/magic-items/srd_wand_of_fear.json"));
        JsonNode section = readJson(files, "/compendium/magic-items/section.json");
        assertEquals("equipment", section.get("dataKind").asText());
    }

    @Test
    void manifestExposesDataKindAndViewForSite() {
        Map<String, Object> manifest = service.manifest();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tree = (List<Map<String, Object>>) manifest.get("tree");
        assertTrue(tree.stream().anyMatch(node -> "spell".equals(node.get("dataKind"))));
        assertTrue(tree.stream().anyMatch(node -> "creature".equals(node.get("dataKind"))));
        assertTrue(tree.stream().anyMatch(node -> "equipment".equals(node.get("dataKind"))));
        assertTrue(tree.stream().allMatch(node -> node.containsKey("view")));
    }

    @Test
    void filtersModuleByRequestedSrdVersion() {
        String srdVersion = "5.2.1";
        Spell spell = new Spell();
        spell.setUrl("spell");
        spell.setLevel(0L);
        when(spellRepository.findAllVisibleForVttgExport(srdVersion)).thenReturn(List.of(spell));
        when(spellMapper.toVttg(spell)).thenReturn(VttgSpell.builder().id("spell").build());

        VttgModuleArchive archive = service.buildSpellModule(srdVersion);

        assertEquals("ttg-club-srd-5-2-1-spells.zip", archive.fileName());
        verify(spellRepository).findAllVisibleForVttgExport(srdVersion);
    }

    private boolean hasFile(Map<String, byte[]> files, String suffix) {
        return files.keySet().stream().anyMatch(path -> path.endsWith(suffix));
    }

    private JsonNode readJson(Map<String, byte[]> files, String suffix) throws Exception {
        byte[] content = files.entrySet().stream()
                .filter(entry -> entry.getKey().endsWith(suffix))
                .findFirst()
                .orElseThrow()
                .getValue();
        return objectMapper.readTree(content);
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

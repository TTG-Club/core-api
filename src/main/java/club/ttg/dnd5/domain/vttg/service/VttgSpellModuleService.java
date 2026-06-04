package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgSpell;
import club.ttg.dnd5.exception.ContentNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class VttgSpellModuleService {
    public static final String DEFAULT_SRD_VERSION = "5.2";

    private final SpellRepository spellRepository;
    private final VttgSpellMapper spellMapper;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public VttgModuleArchive buildModule() {
        String version = DEFAULT_SRD_VERSION;
        String moduleId = "ttg-club-srd-" + version.replaceAll("[^0-9A-Za-z]+", "-").toLowerCase(Locale.ROOT) + "-spells";
        List<VttgSpell> spells = spellRepository.findAllVisibleBySrdVersion(version).stream()
                .map(spellMapper::toVttg)
                .toList();

        if (spells.isEmpty()) {
            throw new ContentNotFoundException("Заклинания SRD " + version + " не найдены");
        }

        return new VttgModuleArchive(moduleId + ".zip", createArchive(moduleId, version, spells));
    }

    private byte[] createArchive(String moduleId, String srdVersion, List<VttgSpell> spells) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            writeJson(zip, moduleId + "/module.json", moduleManifest(moduleId, srdVersion));
            writeText(zip, moduleId + "/client.js", clientScript(moduleId, srdVersion));
            writeJson(zip, moduleId + "/spells.json", spells);
            zip.finish();
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to build VTTG spell module", exception);
        }
    }

    private void writeJson(ZipOutputStream zip, String path, Object value) throws IOException {
        writeBytes(zip, path, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(value));
    }

    private void writeText(ZipOutputStream zip, String path, String value) throws IOException {
        writeBytes(zip, path, value.getBytes(StandardCharsets.UTF_8));
    }

    private void writeBytes(ZipOutputStream zip, String path, byte[] value) throws IOException {
        zip.putNextEntry(new ZipEntry(path));
        zip.write(value);
        zip.closeEntry();
    }

    private Object moduleManifest(String moduleId, String srdVersion) {
        return new Object() {
            public final String id = moduleId;
            public final String name = "Заклинания TTG Club SRD " + srdVersion;
            public final String version = "1.0.0";
            public final String description = "Заклинания SRD " + srdVersion + ", экспортированные с TTG Club";
            public final String author = "TTG Club";
            public final List<String> compatibleSystems = List.of("dnd5e");
            public final List<String> permissions = List.of("notifications");
            public final Object client = new Object() {
                public final String entry = "client.js";
            };
        };
    }

    private String clientScript(String moduleId, String srdVersion) {
        return """
                globalThis.VTTModules.register('%s', async (api) => {
                  const manifest = {
                    id: '%s-compendium',
                    name: 'Заклинания TTG Club SRD %s',
                    tree: [{
                      id: '%s-spells',
                      name: 'Заклинания',
                      icon: 'tabler:sparkles',
                      dataFile: 'spells.json'
                    }]
                  };

                  try {
                    const response = await fetch('/module-assets/%s/spells.json');
                    if (!response.ok) throw new Error(`HTTP ${response.status}`);
                    const spells = await response.json();
                    api.compendium.register('%s', manifest, { 'spells.json': spells });
                    api.notifications.success('TTG Club', `Загружено заклинаний SRD %s: ${spells.length}`);
                  } catch (error) {
                    console.error('[%s] Failed to load spells:', error);
                    api.notifications.error('TTG Club', 'Не удалось загрузить модуль заклинаний');
                  }
                });
                """.formatted(moduleId, moduleId, srdVersion, moduleId, moduleId, moduleId, srdVersion, moduleId);
    }
}

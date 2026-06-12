package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgSpell;
import club.ttg.dnd5.exception.ContentNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class VttgSpellModuleService {
    public static final String SRD_LABEL = "srd";

    private final SpellRepository spellRepository;
    private final VttgSpellMapper spellMapper;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public VttgModuleArchive buildModule() {
        return buildModule(null);
    }

    @Transactional(readOnly = true)
    public VttgModuleArchive buildModule(String requestedSrdVersion) {
        String srdVersion = normalizeSrdVersion(requestedSrdVersion);
        String srdLabel = srdVersion == null ? SRD_LABEL : srdVersion;
        String moduleId = srdVersion == null ? "ttg-club-srd-spells" : "ttg-club-srd-" + slug(srdVersion) + "-spells";
        List<VttgSpell> spells = spellRepository.findAllVisibleForVttgExport(srdVersion).stream()
                .map(spellMapper::toVttg)
                .toList();

        if (spells.isEmpty()) {
            throw new ContentNotFoundException("Заклинания SRD" + (srdVersion == null ? "" : " " + srdVersion) + " не найдены");
        }

        return new VttgModuleArchive(moduleId + ".zip", createArchive(moduleId, srdLabel, spells));
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

    private Map<String, Object> moduleManifest(String moduleId, String srdVersion) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", moduleId);
        result.put("name", "Заклинания TTG Club SRD " + srdVersion);
        result.put("version", "1.0.1");
        result.put("description", "Заклинания SRD " + srdVersion + ", экспортированные с TTG Club");
        result.put("author", "TTG Club");
        result.put("compatibleSystems", List.of("dnd5e"));
        result.put("permissions", List.of("notifications"));
        result.put("client", Map.of("entry", "client.js"));
        result.put("scripts", List.of("client.js"));
        return result;
    }

    private String clientScript(String moduleId, String srdVersion) {
        return """
                (() => {
                  let registered = false;
                  const register = () => {
                    if (registered) return true;
                    const modules = globalThis.VTTModules;
                    if (!modules || typeof modules.register !== 'function') return false;
                    registered = true;
                    modules.register('%s', async (api) => {
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
                        api.notifications?.success?.('TTG Club', `Загружено заклинаний SRD %s: ${spells.length}`);
                      } catch (error) {
                        console.error('[%s] Failed to load spells:', error);
                        api.notifications?.error?.('TTG Club', 'Не удалось загрузить модуль заклинаний');
                      }
                    });
                    return true;
                  };
                  if (!register()) {
                    let attempts = 0;
                    const timer = globalThis.setInterval(() => {
                      attempts += 1;
                      if (register() || attempts >= 50) {
                        globalThis.clearInterval(timer);
                      }
                    }, 100);
                  }
                })();
                """.formatted(moduleId, moduleId, srdVersion, moduleId, moduleId, moduleId, srdVersion, moduleId);
    }

    private String normalizeSrdVersion(String srdVersion) {
        return StringUtils.hasText(srdVersion) ? srdVersion.trim() : null;
    }

    private String slug(String value) {
        return value.replaceAll("[^0-9A-Za-z]+", "-").toLowerCase(Locale.ROOT);
    }
}

package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.repository.CreatureRepository;
import club.ttg.dnd5.domain.common.dictionary.ChallengeRating;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.exception.ContentNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class VttgModuleService {
    public static final String DEFAULT_SRD_VERSION = "5.2";

    private final SpellRepository spellRepository;
    private final CreatureRepository creatureRepository;
    private final VttgSpellMapper spellMapper;
    private final VttgCreatureMapper creatureMapper;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public VttgModuleArchive buildAllModule() {
        return buildModule(Content.ALL);
    }

    @Transactional(readOnly = true)
    public VttgModuleArchive buildSpellModule() {
        return buildModule(Content.SPELLS);
    }

    @Transactional(readOnly = true)
    public VttgModuleArchive buildCreatureModule() {
        return buildModule(Content.CREATURES);
    }

    private VttgModuleArchive buildModule(Content content) {
        String version = DEFAULT_SRD_VERSION;
        String suffix = content.name().toLowerCase(Locale.ROOT);
        String moduleId = "ttg-club-srd-" + version.replaceAll("[^0-9A-Za-z]+", "-").toLowerCase(Locale.ROOT)
                + "-" + suffix;
        Map<String, Object> files = new LinkedHashMap<>();

        if (content != Content.CREATURES) {
            files.put("spells.json", spellRepository.findAllVisibleBySrdVersion(version).stream()
                    .map(spellMapper::toVttg).toList());
        }
        if (content != Content.SPELLS) {
            files.put("creatures.json", groupedCreatures(version));
        }
        if (files.values().stream().allMatch(value -> ((List<?>) value).isEmpty())) {
            throw new ContentNotFoundException("Контент SRD " + version + " не найден");
        }

        return new VttgModuleArchive(moduleId + ".zip", createArchive(moduleId, version, content, files));
    }

    private List<Object> groupedCreatures(String version) {
        List<Creature> creatures = creatureRepository.findAllVisibleBySrdVersion(version).stream()
                .sorted(Comparator
                        .comparing((Creature creature) -> Objects.requireNonNullElse(creature.getExperience(), 0L))
                        .thenComparing(creature -> Objects.requireNonNullElse(creature.getName(), "")))
                .toList();
        List<Object> result = new ArrayList<>();
        String currentChallengeRating = null;

        for (Creature creature : creatures) {
            String challengeRating = ChallengeRating.getCr(Objects.requireNonNullElse(creature.getExperience(), 0L));
            if (!Objects.equals(currentChallengeRating, challengeRating)) {
                currentChallengeRating = challengeRating;
                result.add(Map.of(
                        "type", "separator",
                        "name", "ПО " + challengeRating
                ));
            }
            result.add(creatureMapper.toVttg(creature));
        }

        return result;
    }

    private byte[] createArchive(String moduleId, String version, Content content, Map<String, Object> files) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            writeJson(zip, moduleId + "/module.json", moduleManifest(moduleId, version, content));
            writeText(zip, moduleId + "/client.js", clientScript(moduleId, version, content, files.keySet()));
            for (Map.Entry<String, Object> file : files.entrySet()) {
                writeJson(zip, moduleId + "/" + file.getKey(), file.getValue());
            }
            zip.finish();
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to build VTTG module", exception);
        }
    }

    private Map<String, Object> moduleManifest(String moduleId, String srdVersion, Content content) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", moduleId);
        result.put("name", content.title + " TTG Club SRD " + srdVersion);
        result.put("version", "1.0.1");
        result.put("description", content.title + " SRD " + srdVersion + ", экспортированные с TTG Club");
        result.put("author", "TTG Club");
        result.put("compatibleSystems", List.of("dnd5e"));
        result.put("permissions", List.of("notifications"));
        result.put("client", Map.of("entry", "client.js"));
        result.put("scripts", List.of("client.js"));
        return result;
    }

    private String clientScript(String moduleId, String version, Content content, java.util.Set<String> files) {
        String tree = files.stream()
                .map(file -> {
                    boolean spells = file.equals("spells.json");
                    return "{ id: '%s-%s', name: '%s', icon: '%s', dataFile: '%s' }".formatted(
                            moduleId, spells ? "spells" : "creatures",
                            spells ? "Заклинания" : "Существа",
                            spells ? "tabler:sparkles" : "tabler:skull",
                            file);
                })
                .collect(java.util.stream.Collectors.joining(",\n      "));
        String loads = files.stream()
                .map(file -> "const %s = await load('%s');".formatted(
                        file.equals("spells.json") ? "spells" : "creatures", file))
                .collect(java.util.stream.Collectors.joining("\n    "));
        String data = files.stream()
                .map(file -> "'%s': %s".formatted(file, file.equals("spells.json") ? "spells" : "creatures"))
                .collect(java.util.stream.Collectors.joining(", "));

        return """
                (() => {
                  let registered = false;
                  const register = () => {
                    if (registered) return true;
                    const modules = globalThis.VTTModules;
                    if (!modules || typeof modules.register !== 'function') return false;
                    registered = true;
                    modules.register('%s', async (api) => {
                      const load = async (file) => {
                        const response = await fetch(`/module-assets/%s/${file}`);
                        if (!response.ok) throw new Error(`HTTP ${response.status}`);
                        return response.json();
                      };
                      const manifest = {
                        id: '%s-compendium',
                        name: '%s TTG Club SRD %s',
                        tree: [
                          %s
                        ]
                      };
                      try {
                        %s
                        api.compendium.register('%s', manifest, { %s });
                        api.notifications?.success?.('TTG Club', 'Модуль SRD %s загружен');
                      } catch (error) {
                        console.error('[%s] Failed to load module:', error);
                        api.notifications?.error?.('TTG Club', 'Не удалось загрузить модуль');
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
                """.formatted(moduleId, moduleId, moduleId, content.title, version, tree, loads,
                moduleId, data, version, moduleId);
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

    private enum Content {
        ALL("Контент"),
        SPELLS("Заклинания"),
        CREATURES("Существа");

        private final String title;

        Content(String title) {
            this.title = title;
        }
    }
}

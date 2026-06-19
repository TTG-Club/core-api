package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.repository.CreatureRepository;
import club.ttg.dnd5.domain.common.dictionary.ChallengeRating;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.repository.MagicItemRepository;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgMagicItem;
import club.ttg.dnd5.domain.vttg.service.VttgCompendiumSections.Section;
import club.ttg.dnd5.exception.ContentNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Сборка ZIP-модулей VTTG в файловом формате компендиума «папка на секцию»
 * (CONTENT_AUTHORING.md, раздел 4.1).
 *
 * <p>Структура архива:</p>
 * <pre>
 * &lt;moduleId&gt;/
 * ├── module.json                 // метаданные (без client.entry — это чисто компендиум-модуль)
 * └── compendium/
 *     ├── manifest.json           // тонкий: id/name/readOnly + sections[]
 *     └── &lt;секция&gt;/
 *         ├── section.json         // id/name/icon/dataKind/view
 *         └── … один JSON на сущность (группировка под-папками)
 * </pre>
 *
 * <p>Регистрация компендиума кодом ({@code client.js}/{@code api.compendium.register}), монолитные
 * {@code spells.json}/{@code creatures.json}, поле {@code tree[]} и разделители {@code separator}
 * удалены — сервер VTT сам сканирует {@code compendium/} и подмешивает пак тем же WS-каналом, что и SRD.</p>
 */
@Service
@RequiredArgsConstructor
public class VttgModuleService {
    private static final String COMPENDIUM_DIR = "compendium";

    private final SpellRepository spellRepository;
    private final CreatureRepository creatureRepository;
    private final MagicItemRepository magicItemRepository;
    private final VttgSpellMapper spellMapper;
    private final VttgCreatureMapper creatureMapper;
    private final VttgMagicItemMapper magicItemMapper;
    private final VttgCompendiumSections sections;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public VttgModuleArchive buildAllModule() {
        return buildAllModule(null);
    }

    @Transactional(readOnly = true)
    public VttgModuleArchive buildAllModule(String srdVersion) {
        return buildModule(Content.ALL, srdVersion);
    }

    @Transactional(readOnly = true)
    public VttgModuleArchive buildSpellModule() {
        return buildSpellModule(null);
    }

    @Transactional(readOnly = true)
    public VttgModuleArchive buildSpellModule(String srdVersion) {
        return buildModule(Content.SPELLS, srdVersion);
    }

    @Transactional(readOnly = true)
    public VttgModuleArchive buildCreatureModule() {
        return buildCreatureModule(null);
    }

    @Transactional(readOnly = true)
    public VttgModuleArchive buildCreatureModule(String srdVersion) {
        return buildModule(Content.CREATURES, srdVersion);
    }

    @Transactional(readOnly = true)
    public VttgModuleArchive buildMagicItemModule() {
        return buildMagicItemModule(null);
    }

    @Transactional(readOnly = true)
    public VttgModuleArchive buildMagicItemModule(String srdVersion) {
        return buildModule(Content.MAGIC_ITEMS, srdVersion);
    }

    /**
     * Манифест для контракта сайта {@code VTT_TTG_MANIFEST_PATH}: {@code CompendiumManifest { tree: [...] }}
     * с узлами по каноническому {@code dataKind} и их {@code view}. Используется скачиваемыми паками,
     * чтобы брать «окно и фильтры» с сайта (см. CONTENT_AUTHORING.md, раздел 4).
     */
    public Map<String, Object> manifest() {
        List<Map<String, Object>> tree = sections.all().stream()
                .map(this::treeNode)
                .toList();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tree", tree);
        return result;
    }

    private VttgModuleArchive buildModule(Content content, String requestedSrdVersion) {
        String srdVersion = normalizeSrdVersion(requestedSrdVersion);
        String moduleId = moduleId(srdVersion, content.name().toLowerCase(Locale.ROOT).replace('_', '-'));
        List<SectionPayload> payloads = new ArrayList<>();

        if (content.includes(Content.SPELLS)) {
            addPayload(payloads, sections.spells(), spellRepository.findAllVisibleForVttgExport(srdVersion),
                    this::spellPath, spellMapper::toVttg);
        }
        if (content.includes(Content.CREATURES)) {
            addPayload(payloads, sections.creatures(), creatureRepository.findAllVisibleForVttgExport(srdVersion),
                    this::creaturePath, creatureMapper::toVttg);
        }
        if (content.includes(Content.MAGIC_ITEMS)) {
            addMagicItemPayload(payloads, sections.magicItems(),
                    magicItemRepository.findAllVisibleForVttgExport(srdVersion));
        }

        if (payloads.isEmpty()) {
            throw new ContentNotFoundException(
                    "Контент SRD" + (srdVersion == null ? "" : " " + srdVersion) + " не найден");
        }

        return new VttgModuleArchive(moduleId + ".zip",
                createArchive(moduleId, content.title(srdVersion), payloads));
    }

    private <T> void addPayload(List<SectionPayload> payloads, Section section, List<T> entities,
                                java.util.function.Function<T, String> pathFn,
                                java.util.function.Function<T, Object> mapper) {
        Map<String, Object> entries = new LinkedHashMap<>();
        for (T entity : entities) {
            entries.put(pathFn.apply(entity), mapper.apply(entity));
        }
        if (!entries.isEmpty()) {
            payloads.add(new SectionPayload(section, entries));
        }
    }

    /**
     * Магические предметы: каждый маппится ровно один раз (имя файла берётся из {@code id}
     * уже смапленного объекта), а общий на секцию {@code baseCache} убирает повторные сканы
     * таблицы {@code item} при разрешении базовых предметов зачарований «+1/+2/+3».
     */
    private void addMagicItemPayload(List<SectionPayload> payloads, Section section, List<MagicItem> items) {
        Map<String, List<Item>> baseCache = new HashMap<>();
        Map<String, Object> entries = new LinkedHashMap<>();
        for (MagicItem item : items) {
            // Один предмет может раскрыться в несколько (несколько баз в clarification, напр. «полулаты или латы»).
            for (VttgMagicItem mapped : magicItemMapper.toVttgVariants(item, baseCache)) {
                entries.put(fileName(mapped.getId()), mapped);
            }
        }
        if (!entries.isEmpty()) {
            payloads.add(new SectionPayload(section, entries));
        }
    }

    private String spellPath(Spell spell) {
        return spell.getLevel() + "/" + fileName(spell.getUrl());
    }

    private String creaturePath(Creature creature) {
        String challengeRating = ChallengeRating.getCr(Objects.requireNonNullElse(creature.getExperience(), 0L));
        return "cr/" + slug(challengeRating) + "/" + fileName(creature.getUrl());
    }

    private byte[] createArchive(String moduleId, String packName, List<SectionPayload> payloads) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            String base = moduleId + "/";
            String compendiumBase = base + COMPENDIUM_DIR + "/";

            writeJson(zip, base + "module.json", moduleManifest(moduleId, packName));
            writeJson(zip, compendiumBase + "manifest.json", packManifest(moduleId, packName, payloads));

            for (SectionPayload payload : payloads) {
                String sectionBase = compendiumBase + payload.section().id() + "/";
                writeJson(zip, sectionBase + "section.json", sectionManifest(payload.section()));
                for (Map.Entry<String, Object> entry : payload.entries().entrySet()) {
                    writeJson(zip, sectionBase + entry.getKey(), entry.getValue());
                }
            }

            zip.finish();
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to build VTTG module", exception);
        }
    }

    /** {@code module.json} — без {@code client}/{@code scripts}: модуль не несёт поведения, только компендиум. */
    private Map<String, Object> moduleManifest(String moduleId, String packName) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", moduleId);
        result.put("name", packName);
        result.put("version", "1.1.0");
        result.put("description", packName + ", экспортировано с TTG Club");
        result.put("author", "TTG Club");
        result.put("compatibleSystems", List.of("dnd5e"));
        return result;
    }

    /** Тонкий корневой манифест пака: идентичность + порядок секций (сам {@code view} живёт в section.json). */
    private Map<String, Object> packManifest(String moduleId, String packName, List<SectionPayload> payloads) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", moduleId + "-compendium");
        result.put("name", packName);
        result.put("readOnly", true);
        result.put("sections", payloads.stream().map(payload -> payload.section().id()).toList());
        return result;
    }

    /** {@code section.json}: самодостаточный узел секции ({@code dataFile} выводится из имени папки). */
    private Map<String, Object> sectionManifest(Section section) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", section.id());
        result.put("name", section.name());
        result.put("icon", section.icon());
        result.put("dataKind", section.dataKind());
        if (section.view() != null) {
            result.put("view", section.view());
        }
        return result;
    }

    /** Узел дерева для манифеста сайта: {@code dataKind} + {@code view} + адрес секции. */
    private Map<String, Object> treeNode(Section section) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", section.id());
        result.put("name", section.name());
        result.put("icon", section.icon());
        result.put("dataKind", section.dataKind());
        result.put("dataFile", section.id());
        if (section.view() != null) {
            result.put("view", section.view());
        }
        return result;
    }

    private void writeJson(ZipOutputStream zip, String path, Object value) throws IOException {
        zip.putNextEntry(new ZipEntry(path));
        zip.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(value));
        zip.closeEntry();
    }

    private String normalizeSrdVersion(String srdVersion) {
        return StringUtils.hasText(srdVersion) ? srdVersion.trim() : null;
    }

    /** Имя файла-сущности: {@code <id>.json}, файлобезопасно. */
    private String fileName(String id) {
        String safe = (id == null ? "" : id).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]+", "-");
        safe = safe.replaceAll("^-+|-+$", "");
        return (safe.isEmpty() ? "entity" : safe) + ".json";
    }

    /** Слаг под-папки группировки (напр. ПО "1/2" → "1-2"). */
    private String slug(String value) {
        return value == null ? "" : value.replaceAll("[^0-9A-Za-z]+", "-").toLowerCase(Locale.ROOT);
    }

    private String moduleId(String srdVersion, String suffix) {
        return srdVersion == null
                ? "ttg-club-srd-" + suffix
                : "ttg-club-srd-" + slug(srdVersion) + "-" + suffix;
    }

    /** Сущности одной секции: метаданные + (относительный путь внутри секции → объект записи). */
    private record SectionPayload(Section section, Map<String, Object> entries) {
    }

    private enum Content {
        ALL("Контент"),
        SPELLS("Заклинания"),
        CREATURES("Существа"),
        MAGIC_ITEMS("Магические предметы");

        private final String label;

        Content(String label) {
            this.label = label;
        }

        private boolean includes(Content section) {
            return this == ALL || this == section;
        }

        private String title(String srdVersion) {
            String base = label + " TTG Club SRD";
            return srdVersion == null ? base : base + " " + srdVersion;
        }
    }
}

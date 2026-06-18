package club.ttg.dnd5.domain.vttg.service;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Описания секций компендиума VTTG (формат «папка на секцию», см. CONTENT_AUTHORING.md, раздел 4.1).
 *
 * <p>Единый источник истины для двух потребителей:
 * <ul>
 *   <li>{@link VttgModuleService} — пишет {@code compendium/&lt;секция&gt;/section.json} в ZIP-модуль;</li>
 *   <li>эндпоинт {@code /api/v2/vttg/manifest} (контракт {@code VTT_TTG_MANIFEST_PATH}) — отдаёт
 *       {@code CompendiumManifest { tree: [...] }} с узлами по {@code dataKind} и их {@code view},
 *       чтобы скачиваемые паки брали отображение/фильтры с сайта.</li>
 * </ul>
 *
 * <p>Иконки — только из коллекций {@code tabler}/{@code ttg} в формате {@code tabler:&lt;name&gt;}.</p>
 */
@Component
public class VttgCompendiumSections {

    /**
     * Метаданные секции (узел {@code CompendiumTreeNode} без {@code children}/{@code dataFile}).
     *
     * @param id       имя папки секции (= {@code dataFile} при сборке дерева)
     * @param name     человекочитаемое имя
     * @param icon     иконка (tabler/ttg)
     * @param dataKind канонический тип записей секции (spell/creature/equipment/…)
     * @param view     декларативная конфигурация отображения (или {@code null} → дефолтный список)
     */
    public record Section(String id, String name, String icon, String dataKind, Map<String, Object> view) {
    }

    public Section spells() {
        return new Section("spells", "Заклинания", "tabler:sparkles", "spell", spellView());
    }

    public Section creatures() {
        return new Section("creatures", "Существа", "tabler:paw", "creature", creatureView());
    }

    public Section magicItems() {
        return new Section("magic-items", "Магические предметы", "tabler:wand", "equipment", magicItemView());
    }

    /** Все известные секции — для эндпоинта {@code /manifest}. */
    public List<Section> all() {
        return List.of(spells(), creatures(), magicItems());
    }

    /**
     * Дерево разделов («манифест» отображения) для ответа {@code /changes}.
     *
     * <p>Рекурсивные узлы: ЛИСТ ({@code section}/{@code dataKind}/{@code view}) несёт данные,
     * ГРУППА ({@code group}/{@code children}) — только отображение. Слаги {@code section}
     * совпадают с {@code data.section} записей; пустые листы VTTG отбрасывает.</p>
     */
    public List<Map<String, Object>> changesTree() {
        return List.of(
                leaf("spells", "Заклинания", "tabler:sparkles", "spell", spellView()),
                leaf("creatures", "Существа", "tabler:paw", "creature", creatureView()),
                leaf("species", "Виды", "tabler:users", "species", null),
                leaf("backgrounds", "Предыстории", "tabler:book", "background", null),
                leaf("feats", "Черты", "tabler:star", "feat", null),
                group(List.of(
                        leaf("weapons", "Оружие", "tabler:sword", "weapon", listView()),
                        leaf("armor", "Доспехи", "tabler:shield", "equipment", listView()),
                        leaf("trinkets", "Безделушки", "tabler:backpack", "equipment", listView()),
                        leaf("rings", "Кольца", "tabler:circle", "equipment", listView()),
                        leaf("wands", "Жезлы", "tabler:wand", "equipment", listView()),
                        leaf("wondrous", "Чудесные предметы", "tabler:diamond", "equipment", listView())
                        // Лист "tools" (Инструменты) убран: записей с section "tools" в выгрузке нет.
                        // Вернуть, когда появятся данные инструментов (см. VttgItemMapper.putTool).
                ))
        );
    }

    private Map<String, Object> leaf(String section, String name, String icon, String dataKind, Map<String, Object> view) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("section", section);
        node.put("name", name);
        node.put("icon", icon);
        node.put("dataKind", dataKind);
        if (view != null) {
            node.put("view", view);
        }
        return node;
    }

    private Map<String, Object> group(List<Map<String, Object>> children) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("group", "equipment");
        node.put("name", "Снаряжение");
        node.put("icon", "tabler:briefcase");
        node.put("children", children);
        return node;
    }

    /**
     * Простой список без левой панели и фильтров — для предметных листов
     * (оружие/доспехи/безделушки/кольца/жезлы/чудесные/инструменты).
     */
    private Map<String, Object> listView() {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("layout", "list");
        return view;
    }

    private Map<String, Object> spellView() {
        Map<String, Object> view = filtered();
        view.put("groupBy", groupBy("level", "spellLevel"));
        view.put("filters", List.of(
                enumFilter("level", "Круг", "level", "spellLevel", "badges"),
                togglesFilter(List.of(
                        toggle("Лечение", "predicate", "spellHealing", "tabler:heart-filled", "success"),
                        toggle("Концентрация", "path", "concentration", "tabler:eye", "warning")
                )),
                enumFilter("class", "Класс", "classKeys", "spellClass", "list")
        ));
        return view;
    }

    private Map<String, Object> creatureView() {
        Map<String, Object> view = filtered();
        view.put("groupBy", groupBy("system.challengeRating", "challengeRating"));
        view.put("filters", List.of(
                enumFilter("cr", "ПО", "system.challengeRating", "challengeRating", "badges"),
                enumFilter("type", "Тип", "system.type", "creatureType", "list")
        ));
        return view;
    }

    private Map<String, Object> magicItemView() {
        Map<String, Object> view = filtered();
        view.put("filters", List.of(
                enumFilter("rarity", "Редкость", "rarity", "string", "badges"),
                enumFilter("category", "Категория", "equipmentCategory", "string", "list")
        ));
        return view;
    }

    private Map<String, Object> filtered() {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("layout", "filtered");
        return view;
    }

    private Map<String, Object> groupBy(String path, String format) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("path", path);
        result.put("format", format);
        return result;
    }

    private Map<String, Object> enumFilter(String id, String label, String path, String format, String style) {
        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("id", id);
        filter.put("label", label);
        filter.put("type", "enum");
        filter.put("path", path);
        filter.put("format", format);
        filter.put("style", style);
        return filter;
    }

    private Map<String, Object> togglesFilter(List<Map<String, Object>> toggles) {
        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("id", "props");
        filter.put("label", "Свойства");
        filter.put("type", "toggles");
        filter.put("toggles", toggles);
        return filter;
    }

    /** Один переключатель фильтра {@code toggles}: источник — либо {@code path}, либо {@code predicate}. */
    private Map<String, Object> toggle(String label, String sourceKey, String sourceValue, String icon, String color) {
        Map<String, Object> toggle = new LinkedHashMap<>();
        toggle.put("label", label);
        toggle.put(sourceKey, sourceValue);
        toggle.put("icon", icon);
        toggle.put("color", color);
        return toggle;
    }
}

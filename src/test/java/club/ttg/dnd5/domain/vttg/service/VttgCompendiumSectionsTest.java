package club.ttg.dnd5.domain.vttg.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VttgCompendiumSectionsTest {
    private final VttgCompendiumSections sections = new VttgCompendiumSections();

    @Test
    void groupsFeatsByExportedCategory() {
        Map<String, Object> feats = sections.changesTree().stream()
                .filter(node -> "feats".equals(node.get("section")))
                .findFirst()
                .orElseThrow();

        @SuppressWarnings("unchecked")
        Map<String, Object> view = (Map<String, Object>) feats.get("view");
        @SuppressWarnings("unchecked")
        Map<String, Object> groupBy = (Map<String, Object>) view.get("groupBy");

        assertEquals("list", view.get("layout"));
        assertEquals(Map.of("path", "category", "format", "string"), groupBy);
    }

    /**
     * Инструменты — лист внутри группы «Снаряжение». Без него папка {@code tools} остаётся вне
     * дерева, и VTTG рисует её generic-фоллбэком: «Tools» с иконкой {@code tabler:cards}.
     */
    @Test
    void exposesToolsLeafInsideEquipmentGroup() {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> equipment = (List<Map<String, Object>>) sections.changesTree().stream()
                .filter(node -> "equipment".equals(node.get("group")))
                .findFirst()
                .orElseThrow()
                .get("children");

        Map<String, Object> tools = equipment.stream()
                .filter(node -> "tools".equals(node.get("section")))
                .findFirst()
                .orElseThrow();

        assertEquals("Инструменты", tools.get("name"));
        assertEquals("tabler:tools", tools.get("icon"));
        assertEquals("tool", tools.get("dataKind"));
    }

    /** Глоссарий — отдельный лист дерева: слаг совпадает с {@code section} записей, есть фильтр категорий. */
    @Test
    void exposesGlossaryLeaf() {
        Map<String, Object> glossary = sections.changesTree().stream()
                .filter(node -> "glossary".equals(node.get("section")))
                .findFirst()
                .orElseThrow();

        assertEquals("glossary", glossary.get("dataKind"));

        @SuppressWarnings("unchecked")
        Map<String, Object> view = (Map<String, Object>) glossary.get("view");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> filters = (List<Map<String, Object>>) view.get("filters");

        assertEquals("filtered", view.get("layout"));
        assertEquals(Map.of("path", "category", "format", "string"), view.get("groupBy"));
        assertEquals("category", filters.getFirst().get("path"));
    }
}

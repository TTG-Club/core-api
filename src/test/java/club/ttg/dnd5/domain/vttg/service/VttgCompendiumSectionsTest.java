package club.ttg.dnd5.domain.vttg.service;

import org.junit.jupiter.api.Test;

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
}

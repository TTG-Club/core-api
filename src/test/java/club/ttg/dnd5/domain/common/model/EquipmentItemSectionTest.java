package club.ttg.dnd5.domain.common.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Раздел позиции снаряжения ездит между формой и базой строкой словаря сайта
 * ({@code items} / {@code magic-items}), а не именем константы: этой же строкой
 * форма строит ссылку на карточку, и она же лежит в JSONB инвентаря существа.
 */
@DisplayName("Раздел карточки в позиции снаряжения")
class EquipmentItemSectionTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void writesSectionAsCatalogSlug() {
        EquipmentItem item = new EquipmentItem("flame-tongue", "Огненный язык", 1, null,
                SectionType.MAGIC_ITEM);

        JsonNode json = objectMapper.valueToTree(item);

        assertEquals("magic-items", json.get("section").asText());
        assertEquals("flame-tongue", json.get("url").asText());
    }

    @Test
    void readsSectionFromCatalogSlug() throws Exception {
        EquipmentItem item = objectMapper.readValue(
                "{\"url\":\"dagger-phb\",\"name\":\"Кинжал\",\"section\":\"items\"}",
                EquipmentItem.class
        );

        assertEquals(SectionType.ITEM, item.getSection());
        assertEquals("dagger-phb", item.getUrl());
    }

    /**
     * Стартовое снаряжение предысторий и классов раздела не знает: там выбирают
     * только обычные предметы, и записи старого формата читаться не перестают.
     */
    @Test
    void readsPositionWithoutSection() throws Exception {
        List<EquipmentItem> items = objectMapper.readValue(
                "[{\"url\":\"dagger\",\"name\":\"Кинжал\",\"quantity\":2}]",
                objectMapper.getTypeFactory().constructCollectionType(List.class, EquipmentItem.class)
        );

        assertNull(items.getFirst().getSection());
        assertEquals(2, items.getFirst().getQuantity());
    }

    /** Четырёхаргументный конструктор — та же позиция без раздела. */
    @Test
    void shortConstructorLeavesSectionEmpty() throws Exception {
        EquipmentItem item = new EquipmentItem("dagger", "Кинжал", 2, null);

        assertNull(item.getSection());
        assertFalse(objectMapper.writeValueAsString(item).contains("magic-items"));
    }
}

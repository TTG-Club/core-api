package club.ttg.dnd5.domain.common.rest.mapper;

import club.ttg.dnd5.domain.common.model.EquipmentItem;
import club.ttg.dnd5.domain.common.model.EquipmentOption;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EquipmentMappingTest
{
    private final EquipmentMapping mapping = new EquipmentMappingImpl();

    @Test
    void formGetsTwoEmptyOptionsWhenEquipmentIsNotFilled()
    {
        var options = mapping.toEquipmentForm(null);

        assertEquals(2, options.size());
        assertTrue(options.get(0).getItems().isEmpty());
        assertNull(options.get(0).getCoins());
    }

    @Test
    void formKeepsSavedOptionsAsIs()
    {
        List<EquipmentOption> saved = List.of(coins(75));

        assertEquals(saved, mapping.toEquipmentForm(saved));
    }

    @Test
    void emptyOptionsAndItemsAreNotSaved()
    {
        EquipmentOption filled = new EquipmentOption();
        filled.setItems(new ArrayList<>(List.of(item("dagger", 2, null), item(null, null, null))));

        var options = mapping.toEquipmentEntities(new ArrayList<>(List.of(filled, new EquipmentOption())));

        assertEquals(1, options.size());
        assertEquals(1, options.getFirst().getItems().size());
        assertEquals("dagger", options.getFirst().getItems().getFirst().getUrl());
    }

    @Test
    void itemWithOnlyDescriptionIsSaved()
    {
        EquipmentOption option = new EquipmentOption();
        option.setItems(new ArrayList<>(List.of(item(null, null, "музыкальный инструмент по вашему выбору"))));

        var options = mapping.toEquipmentEntities(new ArrayList<>(List.of(option)));

        assertEquals(1, options.size());
        assertEquals(1, options.getFirst().getItems().size());
    }

    @Test
    void formDefaultsAreNotSaved()
    {
        assertNull(mapping.toEquipmentEntities(mapping.toEquipmentForm(null)));
    }

    @Test
    void responseDerivesLabelsFromOrderAndCoinName()
    {
        EquipmentOption gear = new EquipmentOption();
        gear.setItems(List.of(item("musical-instrument", 1, "по вашему выбору")));
        gear.setCoins(19);

        var response = mapping.toEquipmentOptionDtos(List.of(gear, coins(75)));

        assertEquals("А", response.get(0).getLabel());
        assertEquals("Б", response.get(1).getLabel());
        assertEquals("зм", response.get(0).getCoin());
        assertEquals(19, response.get(0).getCoins());
        assertEquals("по вашему выбору", response.get(0).getItems().getFirst().getDescription());
        assertEquals(1, response.get(0).getItems().getFirst().getQuantity());
        assertEquals(75, response.get(1).getCoins());
    }

    private EquipmentOption coins(int amount)
    {
        EquipmentOption option = new EquipmentOption();
        option.setCoins(amount);
        return option;
    }

    private EquipmentItem item(String url, Integer quantity, String description)
    {
        EquipmentItem item = new EquipmentItem();
        item.setUrl(url);
        item.setQuantity(quantity);
        item.setDescription(description);
        return item;
    }
}

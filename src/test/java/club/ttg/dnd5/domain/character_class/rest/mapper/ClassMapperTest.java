package club.ttg.dnd5.domain.character_class.rest.mapper;

import club.ttg.dnd5.domain.character_class.model.ClassEquipmentItem;
import club.ttg.dnd5.domain.character_class.model.ClassEquipmentOption;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ClassMapperTest
{
    @Mock
    private BaseMapping baseMapping;

    @InjectMocks
    private ClassMapperImpl mapper;

    @Test
    void formGetsTwoEmptyOptionsWhenClassHasNoEquipment()
    {
        var options = mapper.toEquipmentForm(null);

        assertEquals(2, options.size());
        assertTrue(options.get(0).getItems().isEmpty());
        assertNull(options.get(0).getCoins());
    }

    @Test
    void formKeepsSavedOptionsAsIs()
    {
        List<ClassEquipmentOption> saved = List.of(coins(75));

        assertEquals(saved, mapper.toEquipmentForm(saved));
    }

    @Test
    void emptyOptionsAndItemsAreNotSaved()
    {
        ClassEquipmentOption filled = new ClassEquipmentOption();
        filled.setItems(new ArrayList<>(List.of(item("dagger", 2, null), item(null, null, null))));

        var options = mapper.toEquipmentEntities(new ArrayList<>(List.of(filled, new ClassEquipmentOption())));

        assertEquals(1, options.size());
        assertEquals(1, options.getFirst().getItems().size());
        assertEquals("dagger", options.getFirst().getItems().getFirst().getUrl());
    }

    @Test
    void itemWithOnlyDescriptionIsSaved()
    {
        ClassEquipmentOption option = new ClassEquipmentOption();
        option.setItems(new ArrayList<>(List.of(item(null, null, "музыкальный инструмент по вашему выбору"))));

        var options = mapper.toEquipmentEntities(new ArrayList<>(List.of(option)));

        assertEquals(1, options.size());
        assertEquals(1, options.getFirst().getItems().size());
    }

    @Test
    void formDefaultsAreNotSaved()
    {
        assertNull(mapper.toEquipmentEntities(mapper.toEquipmentForm(null)));
    }

    @Test
    void responseDerivesLabelsFromOrderAndCoinName()
    {
        ClassEquipmentOption gear = new ClassEquipmentOption();
        gear.setItems(List.of(item("musical-instrument", 1, "по вашему выбору")));
        gear.setCoins(19);

        var response = mapper.toEquipmentOptionDtos(List.of(gear, coins(75)));

        assertEquals("А", response.get(0).getLabel());
        assertEquals("Б", response.get(1).getLabel());
        assertEquals("зм", response.get(0).getCoin());
        assertEquals(19, response.get(0).getCoins());
        assertEquals("по вашему выбору", response.get(0).getItems().getFirst().getDescription());
        assertEquals(1, response.get(0).getItems().getFirst().getQuantity());
        assertEquals(75, response.get(1).getCoins());
    }

    private ClassEquipmentOption coins(int amount)
    {
        ClassEquipmentOption option = new ClassEquipmentOption();
        option.setCoins(amount);
        return option;
    }

    private ClassEquipmentItem item(String url, Integer quantity, String description)
    {
        ClassEquipmentItem item = new ClassEquipmentItem();
        item.setUrl(url);
        item.setQuantity(quantity);
        item.setDescription(description);
        return item;
    }
}

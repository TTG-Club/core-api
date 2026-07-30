package club.ttg.dnd5.domain.background.rest.mapper;

import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundRequest;
import club.ttg.dnd5.domain.common.model.EquipmentItem;
import club.ttg.dnd5.domain.common.model.EquipmentOption;
import club.ttg.dnd5.domain.common.rest.dto.NameRequest;
import club.ttg.dnd5.domain.common.rest.mapper.EquipmentMappingImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BackgroundMapperTest {
    private final BackgroundMapper mapper = new BackgroundMapperImpl(new EquipmentMappingImpl());

    @Test
    void detailResponseDerivesEquipmentLabels() {
        Background background = new Background();
        background.setAbilities(Set.of());
        background.setSkillProficiencies(Set.of());
        background.setStartingEquipment(List.of(gear(), coins(50)));

        var options = mapper.toDetail(background).getStartingEquipment();

        assertEquals(2, options.size());
        assertEquals("А", options.getFirst().getLabel());
        assertEquals("Б", options.get(1).getLabel());
        assertEquals("зм", options.get(1).getCoin());
        assertEquals(50, options.get(1).getCoins());
        assertEquals("bagpipes", options.getFirst().getItems().getFirst().getUrl());
    }

    @Test
    void formGetsTwoEmptyOptionsWhenEquipmentIsNotFilled() {
        assertEquals(2, mapper.toRequest(new Background()).getStartingEquipment().size());
    }

    @Test
    void emptyOptionsAreNotSaved() {
        BackgroundRequest request = request();
        request.setStartingEquipment(new ArrayList<>(List.of(new EquipmentOption(), new EquipmentOption())));

        assertNull(mapper.toEntity(request, null, null).getStartingEquipment());
    }

    private EquipmentOption gear() {
        EquipmentItem instrument = new EquipmentItem();
        instrument.setUrl("bagpipes");
        instrument.setQuantity(1);

        EquipmentOption option = new EquipmentOption();
        option.setItems(List.of(instrument));
        option.setCoins(11);
        return option;
    }

    private EquipmentOption coins(int amount) {
        EquipmentOption option = new EquipmentOption();
        option.setCoins(amount);
        return option;
    }

    private BackgroundRequest request() {
        BackgroundRequest request = new BackgroundRequest();
        request.setUrl("background");
        request.setName(name());
        request.setAbilityScores(Set.of());
        request.setSkillsProficiencies(Set.of());
        return request;
    }

    private NameRequest name() {
        NameRequest name = new NameRequest();
        name.setName("Предыстория");
        name.setEnglish("Background");
        name.setAlternative(List.of());
        return name;
    }
}

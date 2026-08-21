package club.ttg.dnd5.domain.background.rest.mapper;

import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundRequest;
import club.ttg.dnd5.domain.common.model.EquipmentItem;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.common.model.EquipmentOption;
import club.ttg.dnd5.domain.common.rest.dto.NameRequest;
import club.ttg.dnd5.domain.common.rest.mapper.EquipmentMappingImpl;
import club.ttg.dnd5.dto.base.mapping.BaseMappingImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BackgroundMapperTest {
    private final BackgroundMapper mapper =
            new BackgroundMapperImpl(new EquipmentMappingImpl(), new BaseMappingImpl());

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

    /**
     * Список предысторий показывает черту прямо в строке: за деталью каждой строки
     * ради одного названия ходить нельзя.
     */
    @Test
    void shortResponseCarriesFeat() {
        Feat feat = new Feat();
        feat.setUrl("magic-initiate");
        feat.setName("Посвящённый в магию");

        Background background = new Background();
        background.setAbilities(Set.of());
        background.setFeat(feat);

        var response = mapper.toShort(background);

        assertEquals("Посвящённый в магию", response.getFeatName());
        assertEquals("magic-initiate", response.getFeatUrl());
    }

    /** Предыстория без черты: полям неоткуда взяться, и в ответе их нет. */
    @Test
    void shortResponseWithoutFeatKeepsFieldsNull() {
        Background background = new Background();
        background.setAbilities(Set.of());

        var response = mapper.toShort(background);

        assertNull(response.getFeatName());
        assertNull(response.getFeatUrl());
    }

    @Test
    void emptyOptionsAreNotSaved() {
        BackgroundRequest request = request();
        request.setStartingEquipment(new ArrayList<>(List.of(new EquipmentOption(), new EquipmentOption())));

        assertNull(mapper.toEntity(request, null, null).getStartingEquipment());
    }

    /**
     * Форма редактирования обязана вернуть альтернативные названия: без них
     * сохранение из мастерской затирает их пустым списком.
     */
    @Test
    void formKeepsAlternativeNames() {
        Background background = new Background();
        background.setAlternative("Странник;Бродяга");

        var alternative = mapper.toRequest(background).getName().getAlternative();

        assertEquals(List.of("Странник", "Бродяга"), List.copyOf(alternative));
    }

    /** Запись без альтернативных названий сохраняется, а не падает NPE. */
    @Test
    void entityIsMappedWhenAlternativeNamesAreMissing() {
        BackgroundRequest request = request();
        request.getName().setAlternative(null);

        assertEquals("", mapper.toEntity(request, null, null).getAlternative());
    }

    /** То же для обновления: тело без {@code name.alt} приходит из формы. */
    @Test
    void updateSurvivesMissingAlternativeNames() {
        BackgroundRequest request = request();
        request.getName().setAlternative(null);
        Background background = new Background();

        mapper.updateEntity(request, null, null, background);

        assertEquals("", background.getAlternative());
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

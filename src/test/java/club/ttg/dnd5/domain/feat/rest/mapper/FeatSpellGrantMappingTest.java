package club.ttg.dnd5.domain.feat.rest.mapper;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.feat.model.mechanics.SpellGrant;
import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class FeatSpellGrantMappingTest {

    @Mock
    private BaseMapping baseMapping;

    @InjectMocks
    private FeatMapperImpl mapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void detailExposesGrantedSpells() {
        Feat feat = new Feat();
        feat.setMechanics(dragonmarked());

        FeatDetailResponse response = mapper.toDetail(feat);
        SpellGrant spells = response.getMechanics().getSpells();

        assertEquals(2, spells.getSpells().size());
        assertEquals("light-phb", spells.getSpells().getFirst().getUrl());
        assertEquals(Ability.CHARISMA, spells.getSpellcastingAbility());
    }

    @Test
    void rawFormKeepsGrantOfEntity() {
        Feat feat = new Feat();
        feat.setMechanics(dragonmarked());

        FeatRequest request = mapper.toRequest(feat);

        assertTrue(request.getMechanics().getSpells().getAlwaysPrepared());
    }

    /**
     * Ссылками, а не кругом со школой: те берутся из записи справочника и здесь
     * разошлись бы с каталогом при правке заклинания.
     */
    @Test
    void spellsSerializeAsCatalogRefs() throws Exception {
        SpellGrant spells = new SpellGrant();
        spells.setSpells(List.of(new EntityRef("fly-phb", "Полёт")));

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(spells));

        assertEquals("fly-phb", json.get("spells").get(0).get("url").asText());
        assertEquals("Полёт", json.get("spells").get(0).get("name").asText());
        assertFalse(json.has("level"));
        assertFalse(json.has("school"));
    }

    /** Незаполненные поля в JSON не уходят — как и у прочих блоков механики. */
    @Test
    void emptyFieldsAreOmitted() throws Exception {
        SpellGrant spells = new SpellGrant();
        spells.setSpells(List.of(new EntityRef("fly-phb", "Полёт")));

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(spells));

        assertFalse(json.has("spellcastingAbility"));
        assertFalse(json.has("alwaysPrepared"));
    }

    /** Заклинаний у черты может не быть вовсе — блока тогда нет. */
    @Test
    void mechanicsWithoutSpellsKeepsFieldNull() {
        Feat feat = new Feat();
        feat.setMechanics(new FeatMechanics());

        FeatDetailResponse response = mapper.toDetail(feat);

        assertNull(response.getMechanics().getSpells());
    }

    /** «Отмеченный драконом»: свой набор заклинаний, всегда подготовленных. */
    private static FeatMechanics dragonmarked() {
        SpellGrant spells = new SpellGrant();
        spells.setSpells(
                List.of(new EntityRef("light-phb", "Свет"), new EntityRef("mending-phb", "Починка")));
        spells.setSpellcastingAbility(Ability.CHARISMA);
        spells.setAlwaysPrepared(true);

        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setSpells(spells);
        return mechanics;
    }
}

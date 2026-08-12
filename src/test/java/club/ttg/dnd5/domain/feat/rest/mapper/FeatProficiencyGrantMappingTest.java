package club.ttg.dnd5.domain.feat.rest.mapper;

import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.feat.model.mechanics.ProficiencyGrant;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class FeatProficiencyGrantMappingTest {

    @Mock
    private BaseMapping baseMapping;

    @InjectMocks
    private FeatMapperImpl mapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void detailExposesGrantedProficiencies() {
        Feat feat = new Feat();
        feat.setMechanics(martialWeaponTraining());

        FeatDetailResponse response = mapper.toDetail(feat);
        ProficiencyGrant proficiencies = response.getMechanics().getProficiencies();

        assertEquals(
                Set.of(WeaponCategory.MATERIAL_MELEE, WeaponCategory.MATERIAL_RANGED),
                proficiencies.getWeaponCategories());
    }

    @Test
    void rawFormKeepsGrantOfEntity() {
        Feat feat = new Feat();
        feat.setMechanics(moderatelyArmored());

        FeatRequest request = mapper.toRequest(feat);
        ProficiencyGrant proficiencies = request.getMechanics().getProficiencies();

        assertEquals(Set.of(ArmorCategory.MEDIUM, ArmorCategory.SHIELD), proficiencies.getArmorCategories());
        assertNull(proficiencies.getWeaponCategories());
    }

    /** Незаполненные разделы владений в JSON не уходят — как и у модификаторов. */
    @Test
    void emptySectionsAreOmitted() throws Exception {
        ProficiencyGrant proficiencies = new ProficiencyGrant();
        proficiencies.setArmorCategories(Set.of(ArmorCategory.LIGHT));

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(proficiencies));

        assertEquals("LIGHT", json.get("armorCategories").get(0).asText());
        assertFalse(json.has("weaponCategories"));
        assertFalse(json.has("tools"));
    }

    @Test
    void toolsSerializeAsCatalogRefs() throws Exception {
        ProficiencyGrant proficiencies = new ProficiencyGrant();
        proficiencies.setTools(List.of(new EntityRef("thieves-tools-phb", "Воровские инструменты")));

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(proficiencies));

        assertEquals("thieves-tools-phb", json.get("tools").get(0).get("url").asText());
        assertEquals("Воровские инструменты", json.get("tools").get(0).get("name").asText());
    }

    /** «Обучение воинскому оружию»: воинское оружие целиком, обе половины словаря. */
    private static FeatMechanics martialWeaponTraining() {
        ProficiencyGrant proficiencies = new ProficiencyGrant();
        proficiencies.setWeaponCategories(
                Set.of(WeaponCategory.MATERIAL_MELEE, WeaponCategory.MATERIAL_RANGED));

        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setProficiencies(proficiencies);
        return mechanics;
    }

    /** «Умеренно бронированный»: средние доспехи и щиты. */
    private static FeatMechanics moderatelyArmored() {
        ProficiencyGrant proficiencies = new ProficiencyGrant();
        proficiencies.setArmorCategories(Set.of(ArmorCategory.MEDIUM, ArmorCategory.SHIELD));

        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setProficiencies(proficiencies);
        return mechanics;
    }
}

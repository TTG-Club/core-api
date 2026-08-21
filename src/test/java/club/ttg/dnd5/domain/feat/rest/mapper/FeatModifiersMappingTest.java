package club.ttg.dnd5.domain.feat.rest.mapper;

import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.SenseType;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.common.model.mechanics.DamageAffinity;
import club.ttg.dnd5.domain.common.model.mechanics.DamageDefenseFromChoice;
import club.ttg.dnd5.domain.common.model.mechanics.DamageDefenseKind;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.common.model.mechanics.SheetModifiers;
import club.ttg.dnd5.domain.common.model.mechanics.HitPointsModifier;
import club.ttg.dnd5.domain.common.model.mechanics.SenseGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SpeedModifier;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class FeatModifiersMappingTest {

    @Mock
    private BaseMapping baseMapping;

    @InjectMocks
    private FeatMapperImpl mapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void detailExposesModifiers() {
        Feat feat = new Feat();
        feat.setMechanics(lichAscension());

        FeatDetailResponse response = mapper.toDetail(feat);
        SheetModifiers modifiers = response.getMechanics().getModifiers();

        assertEquals(CreatureType.UNDEAD, modifiers.getCreatureType());
        assertEquals(Set.of(DamageType.NECROTIC, DamageType.POISON), modifiers.getDamage().getResistances());
    }

    @Test
    void rawFormKeepsModifiersOfEntity() {
        Feat feat = new Feat();
        feat.setMechanics(tough());

        FeatRequest request = mapper.toRequest(feat);
        HitPointsModifier hitPoints = request.getMechanics().getModifiers().getHitPoints();

        assertEquals(2, hitPoints.getPerAcquisitionLevel());
        assertEquals(2, hitPoints.getPerLevelAfterAcquisition());
        assertNull(hitPoints.getFlat());
    }

    @Test
    void speedKeepsBothNumbersAndEqualsWalkFlags() throws Exception {
        SpeedModifier speed = new SpeedModifier();
        speed.setWalkBonus(10);
        speed.setClimbEqualsWalk(true);

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(speed));

        assertEquals(10, json.get("walkBonus").asInt());
        assertTrue(json.get("climbEqualsWalk").asBoolean());
        assertFalse(json.has("fly"));
        assertFalse(json.has("flyEqualsWalk"));
    }

    @Test
    void resistanceCanComeFromChoice() throws Exception {
        DamageAffinity damage = new DamageAffinity();
        damage.setResistanceFromChoiceKey("damage-type");

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(damage));

        assertEquals("damage-type", json.get("resistanceFromChoiceKey").asText());
        assertFalse(json.has("resistances"));
    }

    @Test
    void defenseByChoiceKeepsKindAndSurvivesRoundTrip() throws Exception {
        DamageAffinity damage = new DamageAffinity();
        damage.setDefenseChoices(List.of(
                new DamageDefenseFromChoice("damage-type", DamageDefenseKind.IMMUNITY),
                new DamageDefenseFromChoice("second-type", DamageDefenseKind.VULNERABILITY)));

        String serialized = objectMapper.writeValueAsString(damage);
        JsonNode json = objectMapper.readTree(serialized);

        assertEquals("damage-type", json.get("defenseChoices").get(0).get("choiceKey").asText());
        assertEquals("IMMUNITY", json.get("defenseChoices").get(0).get("kind").asText());
        assertEquals("VULNERABILITY", json.get("defenseChoices").get(1).get("kind").asText());
        // Иммунитет по выбору легаси-полем не описать — оно остаётся пустым
        assertFalse(json.has("resistanceFromChoiceKey"));

        DamageAffinity parsed = objectMapper.readValue(serialized, DamageAffinity.class);
        assertEquals(2, parsed.getDefenseChoices().size());
        assertEquals(DamageDefenseKind.IMMUNITY, parsed.getDefenseChoices().get(0).getKind());
    }

    @Test
    void sensesAndConditionImmunitiesSerializeAsDictionaryNames() throws Exception {
        SheetModifiers modifiers = new SheetModifiers();
        modifiers.setSenses(List.of(new SenseGrant(SenseType.BLINDSIGHT, 10)));
        modifiers.setConditionImmunities(Set.of(Condition.POISONED));
        modifiers.setTelepathyRange(120);

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(modifiers));

        assertEquals("BLINDSIGHT", json.get("senses").get(0).get("type").asText());
        assertEquals(10, json.get("senses").get(0).get("range").asInt());
        assertEquals("POISONED", json.get("conditionImmunities").get(0).asText());
        assertEquals(120, json.get("telepathyRange").asInt());
        assertFalse(json.has("armorClassBonus"));
    }

    private static FeatMechanics lichAscension() {
        DamageAffinity damage = new DamageAffinity();
        damage.setResistances(Set.of(DamageType.NECROTIC, DamageType.POISON));

        SheetModifiers modifiers = new SheetModifiers();
        modifiers.setCreatureType(CreatureType.UNDEAD);
        modifiers.setDamage(damage);

        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setModifiers(modifiers);
        return mechanics;
    }

    private static FeatMechanics tough() {
        HitPointsModifier hitPoints = new HitPointsModifier();
        hitPoints.setPerAcquisitionLevel(2);
        hitPoints.setPerLevelAfterAcquisition(2);

        SheetModifiers modifiers = new SheetModifiers();
        modifiers.setHitPoints(hitPoints);

        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setModifiers(modifiers);
        return mechanics;
    }
}

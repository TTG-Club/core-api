package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.source.model.Source;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VttgBackgroundMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VttgBackgroundMapper mapper = new VttgBackgroundMapper(new VttgMarkupConverter(objectMapper));

    /** «Послушник» — характеристики в каноническом порядке, навыки, черта, снаряжение. */
    @Test
    void mapsAcolyteToVttgFormat() {
        Background bg = baseBackground("acolyte", "Послушник", "Acolyte");
        // Намеренно в «перемешанном» порядке — маппер обязан отсортировать характеристики канонически.
        bg.setAbilities(orderedAbilities());
        bg.setSkillProficiencies(orderedSkills(Skill.RELIGION, Skill.INSIGHT));
        bg.setFeat(feat("magic-initiate", "Посвящённый в магию", "Magic Initiate"));
        bg.setEquipment("Инструменты каллиграфа, Книга (молитвенник), Священный символ, 8 зм");

        JsonNode json = json(bg);
        assertEquals("acolyte", json.get("id").asText());
        assertEquals("acolyte", json.get("key").asText());
        assertEquals("Acolyte", json.get("nameEn").asText());
        assertEquals("background", json.get("type").asText());
        assertTrue(json.get("isSRD").asBoolean());

        assertEquals("[\"intelligence\",\"wisdom\",\"charisma\"]",
                json.get("abilityGrant").get("abilities").toString());
        assertEquals("[\"insight\",\"religion\"]",
                json.get("skillGrant").get("skills").toString());

        JsonNode featGrant = json.get("featGrant");
        assertEquals("srd_feat_magic_initiate", featGrant.get("featId").asText());
        assertEquals("Посвящённый в магию", featGrant.get("featName").asText());
        assertEquals("Magic Initiate", featGrant.get("featNameEn").asText());

        assertEquals(1, json.get("equipmentOptions").size());
        assertTrue(json.get("equipmentOptions").get(0).get("description").asText().contains("Священный символ"));
    }

    /** «Преступник» — составной навык slug в camelCase (sleightOfHand). */
    @Test
    void mapsCompoundSkillSlug() {
        Background bg = baseBackground("criminal", "Преступник", "Criminal");
        bg.setSkillProficiencies(orderedSkills(Skill.STEALTH, Skill.SLEIGHT_OF_HAND));
        bg.setFeat(feat("alert", "Бдительный", "Alert"));

        JsonNode json = json(bg);
        assertEquals("[\"sleightOfHand\",\"stealth\"]",
                json.get("skillGrant").get("skills").toString());
        assertEquals("srd_feat_alert", json.get("featGrant").get("featId").asText());
    }

    /** Без черты/снаряжения соответствующие блоки опускаются. */
    @Test
    void omitsAbsentGrants() {
        Background bg = baseBackground("hermit", "Отшельник", "Hermit");

        JsonNode json = json(bg);
        assertFalse(json.has("featGrant"));
        assertFalse(json.has("equipmentOptions"));
        assertFalse(json.has("abilityGrant"));
        assertFalse(json.has("skillGrant"));
    }

    private JsonNode json(Background bg) {
        return objectMapper.valueToTree(mapper.toVttg(bg));
    }

    private Background baseBackground(String url, String name, String english) {
        Background bg = new Background();
        bg.setUrl(url);
        bg.setName(name);
        bg.setEnglish(english);
        bg.setDescription("");
        Source source = new Source();
        source.setAcronym("PHB24");
        bg.setSource(source);
        return bg;
    }

    private Feat feat(String url, String name, String english) {
        Feat feat = new Feat();
        feat.setUrl(url);
        feat.setName(name);
        feat.setEnglish(english);
        return feat;
    }

    private Set<Ability> orderedAbilities() {
        return new LinkedHashSet<>(Set.of(new Ability[]{Ability.WISDOM, Ability.CHARISMA, Ability.INTELLIGENCE}));
    }

    private Set<Skill> orderedSkills(Skill... skills) {
        return new LinkedHashSet<>(Set.of(skills));
    }
}

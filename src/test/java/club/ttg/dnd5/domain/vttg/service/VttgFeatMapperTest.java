package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.domain.source.model.Source;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VttgFeatMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VttgFeatMapper mapper = new VttgFeatMapper(new VttgMarkupConverter(objectMapper));

    /** «Оборона» — постоянные поля типа, ключ/имя источника, повторяемость false. */
    @Test
    void mapsDefenseToVttgFormat() {
        Feat feat = baseFeat("defense", "Оборона", "Defense");
        feat.setCategory(FeatCategory.FIGHTING_STYLE);
        feat.setRepeatability(false);
        feat.setDescription("Когда вы носите доспех, вы получаете бонус +1 к КД.");

        JsonNode json = json(feat);
        assertEquals("srd_feat_defense", json.get("id").asText());
        assertEquals("Оборона", json.get("name").asText());
        assertEquals("Defense", json.get("nameEn").asText());
        assertEquals("feat", json.get("type").asText());
        assertEquals("feats", json.get("section").asText());
        assertEquals("feat", json.get("featureType").asText());
        assertEquals("Черты", json.get("typeLabel").asText());
        assertFalse(json.has("source"));
        assertEquals("phb", json.get("sourceKey").asText());
        assertTrue(json.get("isSRD").asBoolean());
        assertFalse(json.get("repeatable").asBoolean());
        assertTrue(json.get("description").asText().contains("+1 к КД"));
    }

    /** Повторяемая черта — {@code repeatable = true} из {@code repeatability}. */
    @Test
    void mapsRepeatableFeat() {
        Feat feat = baseFeat("ability-score-improvement", "Улучшение характеристик", "Ability Score Improvement");
        feat.setRepeatability(true);

        JsonNode json = json(feat);
        assertEquals("srd_feat_ability_score_improvement", json.get("id").asText());
        assertTrue(json.get("repeatable").asBoolean());
    }

    /** Составное английское имя — id в snake_case: «Two-Weapon Fighting» → srd_feat_two_weapon_fighting. */
    @Test
    void buildsSnakeCaseIdFromCompoundName() {
        Feat feat = baseFeat("two-weapon-fighting", "Сражение двумя оружиями", "Two-Weapon Fighting");

        JsonNode json = json(feat);
        assertEquals("srd_feat_two_weapon_fighting", json.get("id").asText());
    }

    /** Повторяемость null трактуется как false. */
    @Test
    void treatsNullRepeatabilityAsFalse() {
        Feat feat = baseFeat("alert", "Бдительный", "Alert");
        feat.setRepeatability(null);

        JsonNode json = json(feat);
        assertFalse(json.get("repeatable").asBoolean());
    }

    /** Разделители категорий совпадают с эталоном feats.json (id/name) и идут в его порядке. */
    @Test
    void buildsSeparatorsMatchingReference() {
        assertEquals(
                java.util.List.of(
                        FeatCategory.FIGHTING_STYLE, FeatCategory.GENERAL,
                        FeatCategory.ORIGIN, FeatCategory.EPIC_BOON),
                mapper.separatorOrder().subList(0, 4));

        assertEquals("separator", mapper.separator(FeatCategory.FIGHTING_STYLE).get("type"));
        // Разделитель несёт section своего листа, иначе уезжает в фейковую папку "Separator".
        assertEquals("feats", mapper.separator(FeatCategory.FIGHTING_STYLE).get("section"));
        assertEquals("fighting_style", mapper.separator(FeatCategory.FIGHTING_STYLE).get("id"));
        assertEquals("Боевой стиль", mapper.separator(FeatCategory.FIGHTING_STYLE).get("name"));
        assertEquals("origin_feat", mapper.separator(FeatCategory.ORIGIN).get("id"));
        assertEquals("Черта происхождения", mapper.separator(FeatCategory.ORIGIN).get("name"));
        assertEquals("epic_feat", mapper.separator(FeatCategory.EPIC_BOON).get("id"));
    }

    /** Все категории enum покрыты порядком разделителей — ни одна черта не «потеряется». */
    @Test
    void separatorOrderCoversAllCategories() {
        assertTrue(mapper.separatorOrder().containsAll(
                java.util.Arrays.asList(FeatCategory.values())));
    }

    private JsonNode json(Feat feat) {
        return objectMapper.valueToTree(mapper.toVttg(feat));
    }

    private Feat baseFeat(String url, String name, String english) {
        Feat feat = new Feat();
        feat.setUrl(url);
        feat.setName(name);
        feat.setEnglish(english);
        feat.setDescription("");
        Source source = new Source();
        source.setAcronym("PHB24");
        source.setName("PHB 2024");
        feat.setSource(source);
        feat.setSrdVersion("5.1");
        return feat;
    }
}

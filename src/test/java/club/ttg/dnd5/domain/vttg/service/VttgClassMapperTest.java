package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.character_class.model.ArmorProficiency;
import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.model.ClassFeature;
import club.ttg.dnd5.domain.character_class.model.ClassFeatureOption;
import club.ttg.dnd5.domain.character_class.model.ClassFeatureScaling;
import club.ttg.dnd5.domain.character_class.model.ClassTableColumn;
import club.ttg.dnd5.domain.character_class.model.ClassTableItem;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.rest.dto.Name;
import club.ttg.dnd5.domain.character_class.model.SkillProficiency;
import club.ttg.dnd5.domain.character_class.model.WeaponProficiency;
import club.ttg.dnd5.domain.source.model.Source;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VttgClassMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VttgClassMapper mapper = new VttgClassMapper(new VttgMarkupConverter(objectMapper));

    /** «Воин» — базовая механика, владения, развёртка scaling в умения, таблица и вложенный подкласс. */
    @Test
    void mapsFighterToVttgFormat() {
        CharacterClass fighter = baseClass("fighter", "Воин", "Fighter");
        fighter.setHitDice(Dice.d10);
        fighter.setArmorProficiency(new ArmorProficiency(Set.of(ArmorCategory.LIGHT, ArmorCategory.HEAVY), null));
        fighter.setWeaponProficiency(
                new WeaponProficiency(Set.of(WeaponCategory.SIMPLE_MELEE, WeaponCategory.MATERIAL_MELEE), null));
        fighter.setSavingThrows(Set.of(Ability.STRENGTH, Ability.CONSTITUTION));
        fighter.setSkillProficiency(new SkillProficiency(2, List.of(Skill.ATHLETICS, Skill.ANIMAL_HANDLING)));
        fighter.setCasterType(CasterType.NONE);

        ClassFeature secondWind = feature("second-wind", 1, "Второе дыхание", "Восстановите хиты.");
        secondWind.setScaling(List.of(scaling(4, "Второе дыхание (3)", "Три использования.")));
        ClassFeature actionSurge = feature("action-surge", 2, "Всплеск действий", "Дополнительное действие.");
        fighter.setFeatures(List.of(secondWind, actionSurge));
        fighter.setTable(List.of(new ClassTableColumn("Второе дыхание",
                List.of(new ClassTableItem(1, "2"), new ClassTableItem(4, "3")))));

        CharacterClass champion = baseClass("champion", "Чемпион", "Champion");
        champion.setFeatures(List.of(feature("improved-critical", 3, "Улучшенный критический удар", "Крит на 19.")));
        fighter.setSubclasses(List.of(champion));

        JsonNode json = json(fighter);
        assertEquals("class", json.get("type").asText());
        assertEquals("classes", json.get("section").asText());
        // id обязателен для раскладки дельты (routeEntity: <id>.json) и равен key.
        assertEquals("fighter", json.get("id").asText());
        assertEquals("fighter", json.get("key").asText());
        assertEquals("Fighter", json.get("nameEn").asText());
        assertEquals("phb", json.get("sourceKey").asText());
        // isSRD выводится всегда, ключ именно "isSRD"; контент помечается SRD (→ SRD-пак).
        assertTrue(json.has("isSRD"));
        assertTrue(json.get("isSRD").asBoolean());
        assertEquals(10, json.get("hitDie").asInt());

        assertEquals("[\"constitution\",\"strength\"]", sortedInsensitive(json.get("savingThrowProficiencies")));
        assertEquals("[\"animalHandling\",\"athletics\"]", sortedInsensitive(json.get("skillChoices").get("from")));
        assertEquals(2, json.get("skillChoices").get("count").asInt());
        assertTrue(json.get("weaponProficiencies").toString().contains("simple"));
        assertTrue(json.get("weaponProficiencies").toString().contains("martial"));

        // Воин — не заклинатель: spellcasting выводится как null.
        assertTrue(json.get("spellcasting").isNull());
        assertEquals("Воинский архетип", json.get("subclassLabel").asText());
        assertEquals(3, json.get("subclassLevel").asInt());

        // scaling развернулся в отдельное умение «second-wind-4» на 4 уровне.
        List<String> featureKeys = json.get("features").findValuesAsText("key");
        assertTrue(featureKeys.contains("second-wind"));
        assertTrue(featureKeys.contains("second-wind-4"));
        assertTrue(featureKeys.contains("action-surge"));

        // Таблица: 20 строк, бонус мастерства и featureKeys уровня, динамическая колонка.
        JsonNode levelTable = json.get("levelTable");
        assertEquals(20, levelTable.size());
        JsonNode level1 = levelTable.get(0);
        assertEquals(1, level1.get("level").asInt());
        assertEquals(2, level1.get("proficiencyBonus").asInt());
        assertEquals(6, levelTable.get(19).get("proficiencyBonus").asInt());
        assertTrue(level1.get("featureKeys").toString().contains("second-wind"));
        assertEquals(3, levelTable.get(4).get("proficiencyBonus").asInt()); // уровень 5 → бонус мастерства 3
        assertEquals("2", level1.get(columnKey(json)).asText());

        // Подкласс вложен, умения несут subclassKey.
        assertEquals(1, json.get("subclasses").size());
        JsonNode champ = json.get("subclasses").get(0);
        assertEquals("champion", champ.get("key").asText());
        assertEquals(3, champ.get("unlockLevel").asInt());
        assertEquals("champion", champ.get("features").get(0).get("subclassKey").asText());
    }

    /** «Волшебник» — полный заклинатель: spellcasting = {full, intelligence, 1}; варианты умения → choices. */
    @Test
    void mapsCasterSpellcastingAndChoices() {
        CharacterClass wizard = baseClass("wizard", "Волшебник", "Wizard");
        wizard.setHitDice(Dice.d6);
        wizard.setCasterType(CasterType.FULL);

        ClassFeature style = feature("arcane-tradition", 3, "Магическая традиция", "Выберите традицию.");
        ClassFeatureOption option = new ClassFeatureOption();
        option.setKey("evocation");
        Name name = new Name();
        name.setName("Воплощение");
        option.setName(name);
        option.setDescription("Разрушительная магия.");
        style.setOptions(List.of(option));
        wizard.setFeatures(List.of(style));

        JsonNode json = json(wizard);
        assertEquals(6, json.get("hitDie").asInt());
        JsonNode spellcasting = json.get("spellcasting");
        assertFalse(spellcasting.isNull());
        assertEquals("full", spellcasting.get("type").asText());
        assertEquals("intelligence", spellcasting.get("ability").asText());
        assertEquals(1, spellcasting.get("startLevel").asInt());

        JsonNode choices = json.get("features").get(0).get("choices");
        assertEquals(1, choices.size());
        assertEquals("evocation", choices.get(0).get("key").asText());
        assertEquals("Воплощение", choices.get(0).get("name").asText());
    }

    /** isSRD выводится из srdVersion: свой (homebrew) класс без версии SRD → isSRD=false (→ premium-пак). */
    @Test
    void homebrewClassWithoutSrdVersionIsNotSrd() {
        CharacterClass homebrew = baseClass("blood-hunter", "Охотник на нечисть", "Blood Hunter");
        homebrew.setSrdVersion(null);

        assertFalse(json(homebrew).get("isSRD").asBoolean());
    }

    private String columnKey(JsonNode json) {
        // Ключ единственной динамической колонки таблицы (транслит «Второе дыхание»).
        JsonNode columns = json.get("tableColumns");
        return columns.get(0).get("key").asText();
    }

    private JsonNode json(CharacterClass characterClass) {
        return objectMapper.valueToTree(mapper.toVttg(characterClass));
    }

    /** Значения JSON-массива, отсортированные и обёрнутые в кавычки — для стабильного сравнения множеств. */
    private String sortedInsensitive(JsonNode array) {
        List<String> items = new java.util.ArrayList<>();
        array.forEach(node -> items.add(node.asText()));
        items.sort(String::compareTo);
        return items.stream().map(value -> "\"" + value + "\"")
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));
    }

    private CharacterClass baseClass(String url, String name, String english) {
        CharacterClass characterClass = new CharacterClass();
        characterClass.setUrl(url);
        characterClass.setName(name);
        characterClass.setEnglish(english);
        characterClass.setDescription("");
        Source source = new Source();
        source.setAcronym("PHB24");
        source.setName("PHB 2024");
        characterClass.setSource(source);
        characterClass.setSrdVersion("5.1");
        return characterClass;
    }

    private ClassFeature feature(String key, int level, String name, String description) {
        ClassFeature feature = new ClassFeature();
        feature.setKey(key);
        feature.setLevel(level);
        feature.setName(name);
        feature.setDescription(description);
        return feature;
    }

    private ClassFeatureScaling scaling(int level, String name, String description) {
        ClassFeatureScaling scaling = new ClassFeatureScaling();
        scaling.setLevel(level);
        scaling.setName(name);
        scaling.setDescription(description);
        return scaling;
    }
}

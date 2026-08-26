package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceOption;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceType;
import club.ttg.dnd5.domain.common.model.mechanics.DamageAffinity;
import club.ttg.dnd5.domain.common.model.mechanics.MechanicChoice;
import club.ttg.dnd5.domain.common.model.mechanics.ProficiencyGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SheetModifiers;
import club.ttg.dnd5.domain.species.model.mechanics.SpeciesMechanics;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.model.SpeciesFeature;
import club.ttg.dnd5.domain.species.repository.SpeciesRepository;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesSizeDto;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VttgSpeciesMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SpeciesRepository speciesRepository = mock(SpeciesRepository.class);
    private final SpellRepository spellRepository = mock(SpellRepository.class);

    private final VttgSpeciesMapper mapper = new VttgSpeciesMapper(
            new VttgMarkupConverter(objectMapper), speciesRepository, spellRepository);

    {
        // По умолчанию врождённых заклинаний нет — их проверяет отдельный тест
        when(speciesRepository.findInnateSpells(anyString())).thenReturn(List.of());
    }

    /** «Драконорождённый» — тип/размер/скорость, тёмное зрение в grants, умения в features. */
    @Test
    void mapsDragonbornToVttgFormat() {
        Species species = baseSpecies("dragonborn", "Драконорожденный", "Dragonborn");
        species.setType(CreatureType.DRAGON);
        species.setSizes(List.of(size(Size.MEDIUM)));
        species.setSpeed(30);
        species.setDarkVision(60);
        species.setFeatures(List.of(
                new SpeciesFeature("draconic-flight", "Драконий Полёт", "Draconic Flight", "Призрачные крылья.", null)));

        JsonNode json = json(species);
        assertEquals("species", json.get("type").asText());
        assertEquals("species", json.get("section").asText());
        assertEquals("dragonborn", json.get("key").asText());
        // id обязателен для раскладки дельты (routeEntity: <id>.json) и равен key; isSRD → SRD-пак.
        assertEquals("dragonborn", json.get("id").asText());
        assertTrue(json.get("isSRD").asBoolean());
        assertEquals("Dragonborn", json.get("nameEn").asText());
        assertFalse(json.has("source"));
        assertEquals("phb", json.get("sourceKey").asText());
        assertEquals("dragon", json.get("creatureType").asText());
        assertEquals("[\"medium\"]", json.get("size").toString());
        assertEquals(30, json.get("speed").get("walk").asInt());
        assertFalse(json.get("speed").has("fly"));

        assertEquals(1, json.get("grants").size());
        assertEquals("darkvision", json.get("grants").get(0).get("type").asText());
        assertEquals(60, json.get("grants").get(0).get("range").asInt());

        assertEquals(1, json.get("features").size());
        JsonNode feature = json.get("features").get(0);
        assertEquals("draconic-flight", feature.get("key").asText());
        assertEquals("Драконий Полёт", feature.get("name").asText());
        assertTrue(feature.get("description").asText().contains("крылья"));
    }

    /** «Человек» — несколько размеров в порядке источника; вид без тёмного зрения даёт пустой grants. */
    @Test
    void mapsMultipleSizesAndEmptyGrants() {
        Species species = baseSpecies("human", "Человек", "Human");
        species.setType(CreatureType.HUMANOID);
        species.setSizes(List.of(size(Size.SMALL), size(Size.MEDIUM)));
        species.setSpeed(30);

        JsonNode json = json(species);
        assertEquals("[\"small\",\"medium\"]", json.get("size").toString());
        assertTrue(json.get("grants").isArray());
        assertEquals(0, json.get("grants").size());
    }

    /** «Эльф» — происхождения (дочерние виды) сворачиваются в choices умения-происхождения. */
    @Test
    void embedsLineagesAsChoicesOnLineageFeature() {
        Species elf = baseSpecies("elf", "Эльф", "Elf");
        elf.setType(CreatureType.HUMANOID);
        elf.setSizes(List.of(size(Size.MEDIUM)));
        elf.setSpeed(30);
        elf.setFeatures(List.of(
                new SpeciesFeature("trance", "Транс", "Trance", "Не нужно спать.", null),
                new SpeciesFeature("elf-lineage", "Происхождения эльфов", "Elf Lineage", "Выберите происхождение.", null)));

        Species high = baseSpecies("high-elf", "Высший эльф", "High Elf");
        high.setDescription("Магия высших эльфов.");
        high.setFeatures(List.of(new SpeciesFeature("high-magic", "Магия", "Magic", "Престидижитация.", null)));
        Species drow = baseSpecies("drow", "Дроу", "Drow");
        drow.setDescription("Дроу из Подземья.");
        elf.setLineages(List.of(high, drow));

        JsonNode json = json(elf);
        assertEquals(2, json.get("features").size());
        assertFalse(json.get("features").get(0).has("choices"));

        JsonNode lineage = json.get("features").get(1);
        assertEquals("elf-lineage", lineage.get("key").asText());
        JsonNode choices = lineage.get("choices");
        // Сортировка по имени (кириллица): «Высший эльф» < «Дроу».
        assertEquals(2, choices.size());
        assertEquals("high-elf", choices.get(0).get("key").asText());
        assertEquals("Высший эльф", choices.get(0).get("name").asText());
        assertTrue(choices.get(0).get("description").asText().contains("Престидижитация"));
        assertEquals("drow", choices.get(1).get("key").asText());
    }

    /** Идентичность страницы-источника вида. */
    @Test
    void exportsSourcePageIdentity() {
        JsonNode json = json(baseSpecies("elf-phb", "Эльф", "Elf"));

        assertEquals("species", json.get("srcSection").asText());
        assertEquals("elf-phb", json.get("srcUrl").asText());
    }

    /**
     * Снятая в админке пометка SRD приезжает пустой строкой, а не null. Признак читается как
     * «поле не null», поэтому пустая версия нормализуется в null — иначе запись оставалась бы
     * SRD и уезжала не в тот пак.
     */
    @Test
    void treatsBlankSrdVersionAsNotSrd() {
        Species species = baseSpecies("elf-lfl", "Эльф", "Elf");
        species.setSrdVersion("");

        assertFalse(json(species).get("isSRD").asBoolean());

        species.setSrdVersion("5.2");
        assertTrue(json(species).get("isSRD").asBoolean());
    }

    /**
     * «Дварф» — сопротивления умений сводятся в одну награду, владения навыками идут
     * отдельными: у выданного без выбора количество равно списку.
     */
    @Test
    void collectsGrantsFromFeatureMechanics() {
        Species species = baseSpecies("dwarf", "Дварф", "Dwarf");
        species.setType(CreatureType.HUMANOID);
        species.setSizes(List.of(size(Size.MEDIUM)));
        species.setSpeed(30);
        species.setDarkVision(120);

        SpeciesFeature resilience = new SpeciesFeature("dwarven-resilience", "Дварфийская стойкость",
                "Dwarven Resilience", "Сопротивление яду.", null);
        resilience.setMechanics(mechanics(modifiers(DamageType.POISON), null, null));

        SpeciesFeature keenSenses = new SpeciesFeature("keen-senses", "Обострённые чувства",
                "Keen Senses", "Владение Внимательностью.", null);
        keenSenses.setMechanics(mechanics(null, skills(Skill.PERCEPTION), null));

        species.setFeatures(List.of(resilience, keenSenses));

        JsonNode grants = json(species).get("grants");
        assertEquals(3, grants.size());
        assertEquals("darkvision", grants.get(0).get("type").asText());
        assertEquals(120, grants.get(0).get("range").asInt());
        assertEquals("damageDefense", grants.get(1).get("type").asText());
        assertEquals("[{\"damageType\":\"poison\",\"kind\":\"resistance\"}]",
                grants.get(1).get("entries").toString());
        assertEquals("skillProficiency", grants.get(2).get("type").asText());
        assertEquals(1, grants.get(2).get("count").asInt());
        assertEquals("[\"perception\"]", grants.get(2).get("from").toString());
    }

    /** Выбор навыка отдаётся количеством и пулом, а не списком выданного. */
    @Test
    void mapsSkillChoiceAsGrantWithPool() {
        Species species = baseSpecies("gnoll", "Гнолл", "Gnoll");
        species.setSpeed(30);

        MechanicChoice choice = new MechanicChoice();
        choice.setKey("skill");
        choice.setType(ChoiceType.SKILL);
        choice.setOptions(List.of(new ChoiceOption("PERCEPTION", "Внимательность"),
                new ChoiceOption("STEALTH", "Скрытность"),
                new ChoiceOption("SURVIVAL", "Выживание")));

        SpeciesFeature feature = new SpeciesFeature("hunters-senses", "Чувства охотника",
                "Hunter's Senses", "Один навык на выбор.", null);
        feature.setMechanics(mechanics(null, null, List.of(choice)));
        species.setFeatures(List.of(feature));

        JsonNode grant = json(species).get("grants").get(0);
        assertEquals("skillProficiency", grant.get("type").asText());
        assertEquals(1, grant.get("count").asInt());
        assertEquals("[\"perception\",\"stealth\",\"survival\"]", grant.get("from").toString());
    }

    /**
     * Уровень умения уезжает в {@code level}. Первый уровень не отдаётся: это значение
     * по умолчанию у потребителя, и проставлять его каждому умению незачем.
     */
    @Test
    void exportsFeatureLevel() {
        Species species = baseSpecies("high-elf", "Высший эльф", "High Elf");
        species.setSpeed(30);

        SpeciesFeature magic = new SpeciesFeature("elf-lineage", "Эльфийское происхождение",
                "Elf Lineage", "Туманный шаг с 5 уровня.", null);
        magic.setLevel(5);

        SpeciesFeature trance = new SpeciesFeature("trance", "Транс", "Trance", "Не нужно спать.", null);
        trance.setLevel(1);

        species.setFeatures(List.of(magic, trance));

        JsonNode features = json(species).get("features");
        assertEquals(5, features.get(0).get("level").asInt());
        assertFalse(features.get(1).has("level"));
    }

    /**
     * «Инфернальный тифлинг» — происхождение без умений: награда приходит из механики
     * самой записи, иначе взять её неоткуда.
     */
    @Test
    void collectsGrantsFromSpeciesMechanics() {
        Species lineage = baseSpecies("tiefling-infernal", "Инфернальный тифлинг", "Infernal");
        lineage.setSpeed(30);
        lineage.setMechanics(mechanics(modifiers(DamageType.FIRE), null, null));

        JsonNode grants = json(lineage).get("grants");
        assertEquals(1, grants.size());
        assertEquals("damageDefense", grants.get(0).get("type").asText());
        assertEquals("[{\"damageType\":\"fire\",\"kind\":\"resistance\"}]",
                grants.get(0).get("entries").toString());
    }

    /** Механика записи и механика её умений складываются в одну награду. */
    @Test
    void mergesSpeciesAndFeatureResistances() {
        Species species = baseSpecies("tiefling", "Тифлинг", "Tiefling");
        species.setSpeed(30);
        species.setMechanics(mechanics(modifiers(DamageType.FIRE), null, null));

        SpeciesFeature feature = new SpeciesFeature("infernal-legacy", "Наследие",
                "Infernal Legacy", "Сопротивление яду.", null);
        feature.setMechanics(mechanics(modifiers(DamageType.POISON), null, null));
        species.setFeatures(List.of(feature));

        JsonNode grants = json(species).get("grants");
        assertEquals(1, grants.size());
        // Порядок алфавитный по типу урона: fire идёт раньше poison.
        assertEquals("[{\"damageType\":\"fire\",\"kind\":\"resistance\"},"
                        + "{\"damageType\":\"poison\",\"kind\":\"resistance\"}]",
                grants.get(0).get("entries").toString());
    }

    private SpeciesMechanics mechanics(SheetModifiers modifiers,
                                              ProficiencyGrant proficiencies,
                                              List<MechanicChoice> choices) {
        SpeciesMechanics mechanics = new SpeciesMechanics();
        mechanics.setModifiers(modifiers);
        mechanics.setProficiencies(proficiencies);
        mechanics.setChoices(choices);
        return mechanics;
    }

    private SheetModifiers modifiers(DamageType resistance) {
        DamageAffinity damage = new DamageAffinity();
        damage.setResistances(Set.of(resistance));
        SheetModifiers modifiers = new SheetModifiers();
        modifiers.setDamage(damage);
        return modifiers;
    }

    private ProficiencyGrant skills(Skill... skills) {
        ProficiencyGrant grant = new ProficiencyGrant();
        grant.setSkills(Set.of(skills));
        return grant;
    }

    private JsonNode json(Species species) {
        return objectMapper.valueToTree(mapper.toVttg(species));
    }

    private Species baseSpecies(String url, String name, String english) {
        Species species = new Species();
        species.setUrl(url);
        species.setName(name);
        species.setEnglish(english);
        species.setDescription("");
        Source source = new Source();
        source.setAcronym("PHB24");
        source.setName("PHB 2024");
        species.setSource(source);
        species.setSrdVersion("5.1");
        return species;
    }

    private SpeciesSizeDto size(Size type) {
        SpeciesSizeDto dto = new SpeciesSizeDto();
        dto.setType(type);
        return dto;
    }
}

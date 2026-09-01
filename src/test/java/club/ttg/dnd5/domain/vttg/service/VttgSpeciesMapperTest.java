package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceOption;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceType;
import club.ttg.dnd5.domain.common.model.mechanics.DamageAffinity;
import club.ttg.dnd5.domain.common.model.mechanics.GrantedSpellRef;
import club.ttg.dnd5.domain.common.model.mechanics.MechanicChoice;
import club.ttg.dnd5.domain.common.dictionary.SenseType;
import club.ttg.dnd5.domain.common.model.mechanics.ProficiencyGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SenseGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SheetModifiers;
import club.ttg.dnd5.domain.species.model.mechanics.SpeciesMechanics;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.model.SpeciesFeature;
import club.ttg.dnd5.domain.species.repository.SpeciesRepository;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesSizeDto;
import club.ttg.dnd5.domain.spell.model.Spell;
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
    private final VttgMarkupConverter markupConverter = new VttgMarkupConverter(objectMapper);

    private final VttgSpeciesMapper mapper = new VttgSpeciesMapper(
            markupConverter, speciesRepository, spellRepository,
            new VttgFeatMechanicsMapper(markupConverter, spellRepository,
                    mock(FeatRepository.class)));

    {
        // По умолчанию врождённых заклинаний нет — их проверяет отдельный тест
        when(speciesRepository.findInnateSpells(anyString())).thenReturn(List.of());
    }

    /** «Драконорождённый» — тип/размер/скорость, тёмное зрение в featData своего умения. */
    @Test
    void mapsDragonbornToVttgFormat() {
        Species species = baseSpecies("dragonborn", "Драконорожденный", "Dragonborn");
        species.setType(CreatureType.DRAGON);
        species.setSizes(List.of(size(Size.MEDIUM)));
        species.setSpeed(30);
        species.setFeatures(List.of(
                new SpeciesFeature("draconic-flight", "Драконий Полёт", "Draconic Flight", "Призрачные крылья.", null),
                darkVisionFeature(60)));

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

        assertFalse(json.has("parentKey"));

        assertEquals(2, json.get("features").size());
        JsonNode feature = json.get("features").get(0);
        assertEquals("draconic-flight", feature.get("key").asText());
        assertEquals("Драконий Полёт", feature.get("name").asText());
        assertTrue(feature.get("description").asText().contains("крылья"));

        // Тёмное зрение — дар своего умения, а не записи целиком
        JsonNode darkVisionFeature = json.get("features").get(1);
        assertEquals(60, darkVisionFeature.get("featData").get("darkvision").asInt());
        assertFalse(json.has("featData"));
    }

    /** «Человек» — несколько размеров в порядке источника; запись без механики даров не несёт. */
    @Test
    void mapsMultipleSizesWithoutFeatData() {
        Species species = baseSpecies("human", "Человек", "Human");
        species.setType(CreatureType.HUMANOID);
        species.setSizes(List.of(size(Size.SMALL), size(Size.MEDIUM)));
        species.setSpeed(30);

        JsonNode json = json(species);
        assertEquals("[\"small\",\"medium\"]", json.get("size").toString());
        assertFalse(json.has("featData"));
    }

    /**
     * «Эльф» — происхождения больше не сворачиваются в choices: каждое уезжает
     * самостоятельной записью со ссылкой {@code parentKey} на родителя, а умение
     * «Происхождения эльфов» остаётся обычным текстовым умением.
     */
    @Test
    void exportsLineageAsSeparateRecordWithParentKey() {
        Species elf = baseSpecies("elf", "Эльф", "Elf");
        elf.setType(CreatureType.HUMANOID);
        elf.setSizes(List.of(size(Size.MEDIUM)));
        elf.setSpeed(30);
        elf.setFeatures(List.of(
                new SpeciesFeature("trance", "Транс", "Trance", "Не нужно спать.", null),
                new SpeciesFeature("elf-lineage", "Происхождения эльфов", "Elf Lineage", "Выберите происхождение.", null)));

        Species high = baseSpecies("high-elf", "Высший эльф", "High Elf");
        high.setDescription("Магия высших эльфов.");
        high.setParent(elf);
        high.setFeatures(List.of(new SpeciesFeature("high-magic", "Магия", "Magic", "Престидижитация.", null)));

        JsonNode parentJson = json(elf);
        assertEquals(2, parentJson.get("features").size());
        assertFalse(parentJson.get("features").get(0).has("choices"));
        assertFalse(parentJson.get("features").get(1).has("choices"));
        assertFalse(parentJson.has("parentKey"));

        JsonNode lineageJson = json(high);
        assertEquals("high-elf", lineageJson.get("key").asText());
        assertEquals("elf", lineageJson.get("parentKey").asText());
        assertEquals(1, lineageJson.get("features").size());
        assertEquals("high-magic", lineageJson.get("features").get(0).get("key").asText());
    }

    /** Обычное зрение уезжает числом в футах; не задано — поля нет, токен оставит своё. */
    @Test
    void exportsNormalVision() {
        Species species = baseSpecies("human", "Человек", "Human");
        species.setSpeed(30);

        assertFalse(json(species).has("vision"));

        // Ноль — «без ограничений» и у справочника, и у токена: уезжает нулём
        species.setVision(0);
        assertEquals(0, json(species).get("vision").asInt());

        species.setVision(120);
        assertEquals(120, json(species).get("vision").asInt());
    }

    /** Рост по размерам: ключи те же, что у {@code size}; пустые границы не уезжают. */
    @Test
    void exportsHeightsBySize() {
        Species species = baseSpecies("human", "Человек", "Human");
        species.setSpeed(30);
        species.setSizes(List.of(size(Size.MEDIUM), size(Size.SMALL)));

        // Рост не задан ни одному размеру — поля в записи нет вовсе
        assertFalse(json(species).has("heights"));

        species.setSizes(List.of(sized(Size.MEDIUM, (short) 4, (short) 6), size(Size.SMALL)));

        JsonNode heights = json(species).get("heights");
        assertEquals(4, heights.get("medium").get("from").asInt());
        assertEquals(6, heights.get("medium").get("to").asInt());
        // Размер без границ в карту не попадает, хотя в size он есть
        assertFalse(heights.has("small"));
        assertEquals("[\"medium\",\"small\"]", json(species).get("size").toString());
    }

    /** Одна граница роста — вторая просто опускается; ноль значит «не задано». */
    @Test
    void exportsSingleHeightBound() {
        Species species = baseSpecies("goliath", "Голиаф", "Goliath");
        species.setSpeed(35);
        species.setSizes(List.of(sized(Size.MEDIUM, (short) 5, null)));

        JsonNode medium = json(species).get("heights").get("medium");
        assertEquals(5, medium.get("from").asInt());
        assertFalse(medium.has("to"));

        // Ноль из формы — «не задано», как и у обычного зрения
        species.setSizes(List.of(sized(Size.MEDIUM, (short) 0, (short) 0)));
        assertFalse(json(species).has("heights"));
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
     * «Дварф» — механика каждого умения уезжает его собственным {@code featData}: тёмное
     * зрение и сопротивление у «Дварфийской стойкости», владение навыком у «Обострённых
     * чувств». В общий блок записи ничего не сводится — потребителю важно, какое умение
     * что дало.
     */
    @Test
    void featureMechanicsBecomeFeatureFeatData() {
        Species species = baseSpecies("dwarf", "Дварф", "Dwarf");
        species.setType(CreatureType.HUMANOID);
        species.setSizes(List.of(size(Size.MEDIUM)));
        species.setSpeed(30);

        SheetModifiers resilienceModifiers = modifiers(DamageType.POISON);
        resilienceModifiers.setSenses(List.of(new SenseGrant(SenseType.DARKVISION, 120)));
        SpeciesFeature resilience = new SpeciesFeature("dwarven-resilience", "Дварфийская стойкость",
                "Dwarven Resilience", "Сопротивление яду.", null);
        resilience.setMechanics(mechanics(resilienceModifiers, null, null));

        SpeciesFeature keenSenses = new SpeciesFeature("keen-senses", "Обострённые чувства",
                "Keen Senses", "Владение Внимательностью.", null);
        keenSenses.setMechanics(mechanics(null, skills(Skill.PERCEPTION), null));

        species.setFeatures(List.of(resilience, keenSenses));

        JsonNode json = json(species);
        assertFalse(json.has("featData"));

        JsonNode resilienceData = json.get("features").get(0).get("featData");
        assertEquals(120, resilienceData.get("darkvision").asInt());
        assertEquals("[{\"damageType\":\"poison\",\"kind\":\"resistance\"}]",
                resilienceData.get("damageDefenses").toString());

        JsonNode keenSensesData = json.get("features").get(1).get("featData");
        assertEquals("[\"perception\"]", keenSensesData.get("skillProficiencies").toString());
    }

    /** Выбор навыка уезжает выбором в {@code featData.choices} умения — с пулом вариантов. */
    @Test
    void mapsSkillChoiceIntoFeatureFeatData() {
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

        JsonNode choices = json(species).get("features").get(0).get("featData").get("choices");
        assertEquals(1, choices.size());
        assertEquals("skill", choices.get(0).get("type").asText());
        assertEquals("skill", choices.get(0).get("key").asText());
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
     * «Инфернальный тифлинг» — происхождение без умений: дар приходит из механики самой
     * записи блоком {@code featData}, иначе взять его неоткуда.
     */
    @Test
    void speciesMechanicsBecomeRecordFeatData() {
        Species lineage = baseSpecies("tiefling-infernal", "Инфернальный тифлинг", "Infernal");
        lineage.setSpeed(30);
        lineage.setMechanics(mechanics(modifiers(DamageType.FIRE), null, null));

        JsonNode featData = json(lineage).get("featData");
        assertEquals("[{\"damageType\":\"fire\",\"kind\":\"resistance\"}]",
                featData.get("damageDefenses").toString());
    }

    /**
     * Механика записи и механика умения не сводятся в одно: у каждой свой блок
     * {@code featData} — потребителю важно, какой источник что дал.
     */
    @Test
    void recordAndFeatureFeatDataStaySeparate() {
        Species species = baseSpecies("tiefling", "Тифлинг", "Tiefling");
        species.setSpeed(30);
        species.setMechanics(mechanics(modifiers(DamageType.FIRE), null, null));

        SpeciesFeature feature = new SpeciesFeature("infernal-legacy", "Наследие",
                "Infernal Legacy", "Сопротивление яду.", null);
        feature.setMechanics(mechanics(modifiers(DamageType.POISON), null, null));
        species.setFeatures(List.of(feature));

        JsonNode json = json(species);
        assertEquals("[{\"damageType\":\"fire\",\"kind\":\"resistance\"}]",
                json.get("featData").get("damageDefenses").toString());
        assertEquals("[{\"damageType\":\"poison\",\"kind\":\"resistance\"}]",
                json.get("features").get(0).get("featData").get("damageDefenses").toString());
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

    /** Заклинания умения уезжают у самого умения, а не отдельной записью. */
    @Test
    void mapsFeatureGrantedSpells() {
        Species species = baseSpecies("high-elf", "Высший эльф", "High Elf");
        SpeciesFeature feature = new SpeciesFeature(
                "elven-lineage", "Эльфийское наследие", "Elven Lineage", "Заговор на выбор.", null);
        feature.setGrantedSpells(List.of(new GrantedSpellRef("prestidigitation", null, null)));
        species.setFeatures(List.of(feature));

        when(spellRepository.findAllShortByUrlIn(Set.of("prestidigitation")))
                .thenReturn(List.of(spell("prestidigitation", "Фокусы")));

        JsonNode features = json(species).get("features");
        assertEquals(1, features.size());
        assertEquals("elven-lineage", features.get(0).get("key").asText());

        JsonNode granted = features.get(0).get("grantedSpells");
        assertEquals(1, granted.size());
        assertEquals("Фокусы", granted.get(0).get("name").asText());
        assertEquals("prestidigitation", granted.get(0).get("spellId").asText());
    }

    /** Ссылка на удалённое заклинание блок не роняет и не уезжает пустой записью. */
    @Test
    void skipsFeatureGrantedSpellsMissingFromCatalog() {
        Species species = baseSpecies("tiefling", "Тифлинг", "Tiefling");
        SpeciesFeature feature = new SpeciesFeature(
                "fiendish-legacy", "Наследие преисподней", "Fiendish Legacy", "Магия.", null);
        feature.setGrantedSpells(List.of(new GrantedSpellRef("removed-spell", null, null)));
        species.setFeatures(List.of(feature));

        when(spellRepository.findAllShortByUrlIn(Set.of("removed-spell"))).thenReturn(List.of());

        JsonNode features = json(species).get("features");
        assertEquals(1, features.size());
        assertFalse(features.get(0).has("grantedSpells"));
    }

    private Spell spell(String url, String name) {
        Spell spell = new Spell();
        spell.setUrl(url);
        spell.setName(name);
        return spell;
    }

    private SheetModifiers modifiers(DamageType resistance) {
        DamageAffinity damage = new DamageAffinity();
        damage.setResistances(Set.of(resistance));
        SheetModifiers modifiers = new SheetModifiers();
        modifiers.setDamage(damage);
        return modifiers;
    }

    /** Умение «Тёмное зрение» с чувством DARKVISION заданной дальности — как в справочнике. */
    private SpeciesFeature darkVisionFeature(int range) {
        SheetModifiers modifiers = new SheetModifiers();
        modifiers.setSenses(List.of(new SenseGrant(SenseType.DARKVISION, range)));
        SpeciesFeature feature = new SpeciesFeature("darkvision", "Тёмное зрение", "Darkvision",
                "Тёмное зрение %d фт.".formatted(range), null);
        feature.setMechanics(mechanics(modifiers, null, null));
        return feature;
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

    /** Размер с ростом: границы задаются как в мастерской — любая из двух может быть пустой. */
    private SpeciesSizeDto sized(Size type, Short from, Short to) {
        SpeciesSizeDto dto = size(type);
        dto.setFrom(from);
        dto.setTo(to);
        return dto;
    }
}

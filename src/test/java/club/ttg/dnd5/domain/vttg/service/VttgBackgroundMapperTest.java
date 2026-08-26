package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Coin;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.dictionary.Language;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.background.model.BackgroundToolChoice;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.common.model.EquipmentItem;
import club.ttg.dnd5.domain.common.model.EquipmentOption;
import club.ttg.dnd5.domain.common.model.mechanics.ProficiencyGrant;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VttgBackgroundMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VttgMarkupConverter markupConverter = new VttgMarkupConverter(objectMapper);
    private final FeatRepository featRepository = mock(FeatRepository.class);
    private final VttgBackgroundMapper mapper = new VttgBackgroundMapper(markupConverter,
            new VttgEquipmentMapper(markupConverter),
            new VttgFeatMechanicsMapper(markupConverter, mock(SpellRepository.class)),
            featRepository);

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
        assertEquals("backgrounds", json.get("section").asText());
        assertTrue(json.get("isSRD").asBoolean());

        assertEquals("[\"intelligence\",\"wisdom\",\"charisma\"]",
                json.get("abilityGrant").get("abilities").toString());
        assertEquals("[\"insight\",\"religion\"]",
                json.get("skillGrant").get("skills").toString());

        JsonNode featGrant = json.get("featGrant");
        assertEquals("srd_feat_magic_initiate", featGrant.get("featId").asText());
        assertEquals("Посвящённый в магию", featGrant.get("featName").asText());
        assertEquals("Magic Initiate", featGrant.get("featNameEn").asText());
        // Уточнения у «Послушника» нет — поле опускается, а не едет пустым
        assertFalse(featGrant.has("featSuffix"));

        assertEquals(1, json.get("equipmentOptions").size());
        assertTrue(json.get("equipmentOptions").get(0).get("description").asText().contains("Священный символ"));
    }

    /**
     * «Мудрец» даёт «Посвящённого в магию (Волшебник)»: класс списка заклинаний назван
     * самой предысторией. В названии черты его нет — оно приходит из каталога, — поэтому
     * уточнение едет своим полем, иначе потребитель спросил бы список у игрока заново.
     */
    @Test
    void mapsFeatSuffixAsSeparateField() {
        Background bg = baseBackground("sage", "Мудрец", "Sage");
        bg.setFeat(feat("magic-initiate", "Посвящённый в магию", "Magic Initiate"));
        bg.setFeatSuffix("(Волшебник)");

        JsonNode featGrant = json(bg).get("featGrant");
        assertEquals("Посвящённый в магию", featGrant.get("featName").asText());
        // Скобки — оформление страницы, а не часть значения
        assertEquals("Волшебник", featGrant.get("featSuffix").asText());
    }

    /** Уточнение без скобок доезжает как есть: в модели оно хранится обеими формами. */
    @Test
    void keepsFeatSuffixWithoutBrackets() {
        Background bg = baseBackground("acolyte", "Послушник", "Acolyte");
        bg.setFeat(feat("magic-initiate", "Посвящённый в магию", "Magic Initiate"));
        bg.setFeatSuffix(" Жрец ");

        assertEquals("Жрец", json(bg).get("featGrant").get("featSuffix").asText());
    }

    /** Пустое уточнение полем не едет: {@code NON_NULL} и так вырезал бы его. */
    @Test
    void omitsBlankFeatSuffix() {
        Background bg = baseBackground("criminal", "Преступник", "Criminal");
        bg.setFeat(feat("alert", "Бдительный", "Alert"));
        bg.setFeatSuffix("   ");

        assertFalse(json(bg).get("featGrant").has("featSuffix"));
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

    /**
     * Блоки-списки присутствуют пустыми даже без данных: мастер предыстории в VTTG
     * читает {@code toolGrant.items}/{@code skillGrant.skills} напрямую и падает,
     * если блок вырезан по {@code NON_NULL}. Опускается только {@code featGrant}.
     */
    @Test
    void emitsEmptyGrantBlocksWhenDataAbsent() {
        Background bg = baseBackground("hermit", "Отшельник", "Hermit");

        JsonNode json = json(bg);
        assertFalse(json.has("featGrant"));
        assertEquals("[]", json.get("abilityGrant").get("abilities").toString());
        assertEquals("[]", json.get("skillGrant").get("skills").toString());
        assertEquals("[]", json.get("toolGrant").get("items").toString());
        assertEquals("[]", json.get("equipmentOptions").toString());
    }

    /**
     * Владение инструментами уезжает текстом, как хранится в модели: словарь
     * инструментов живёт на стороне VTTG, там же текст и сопоставляется с ключами.
     */
    @Test
    void emitsToolProficiencyAsText() {
        Background bg = baseBackground("acolyte", "Послушник", "Acolyte");
        bg.setAbilities(orderedAbilities());
        bg.setSkillProficiencies(orderedSkills(Skill.RELIGION, Skill.INSIGHT));
        bg.setFeat(feat("magic-initiate", "Посвящённый в магию", "Magic Initiate"));
        bg.setToolProficiency("{@item Инструменты каллиграфа|url:calligraphers-supplies}");

        JsonNode items = json(bg).get("toolGrant").get("items");
        assertEquals(1, items.size());
        assertEquals(
                "[Инструменты каллиграфа](https://ttg.club/items/calligraphers-supplies)",
                items.get(0).asText()
        );
    }

    /** Без владения инструментами блок остаётся, но пустым. */
    @Test
    void emitsEmptyToolGrantWithoutProficiency() {
        Background bg = baseBackground("hermit", "Отшельник", "Hermit");

        assertEquals("[]", json(bg).get("toolGrant").get("items").toString());
    }

    /**
     * Структурированное снаряжение — основной источник: предметы уезжают ссылками на
     * карточки сайта, вариант «только монеты» получает золотой эквивалент.
     */
    @Test
    void mapsStructuredStartingEquipment() {
        Background bg = baseBackground("criminal", "Преступник", "Criminal");
        bg.setEquipment("Свободный текст, который не должен победить структуру");
        bg.setStartingEquipment(List.of(
                option(List.of(
                        item("dagger", "Кинжал", 2, null),
                        item("thieves-tools", "Воровские инструменты", null, null),
                        item(null, null, null, "древние карты"),
                        item("parchment", "Пергамент", null, "10 листов")
                ), 16),
                option(List.of(), 50)
        ));

        JsonNode options = json(bg).get("equipmentOptions");
        assertEquals(2, options.size());
        assertEquals(
                "2 [Кинжал](https://ttg.club/items/dagger), "
                        + "[Воровские инструменты](https://ttg.club/items/thieves-tools), "
                        + "древние карты, "
                        + "[Пергамент](https://ttg.club/items/parchment) (10 листов), "
                        + "16 зм",
                options.get(0).get("description").asText()
        );
        // Вариант с предметами альтернативой золотом не является.
        assertFalse(options.get(0).has("goldAlternative"));

        assertEquals("50 зм", options.get(1).get("description").asText());
        assertEquals(50, options.get(1).get("goldAlternative").asInt());
    }

    /** Без структуры остаётся легаси-текст — с разобранной разметкой. */
    @Test
    void fallsBackToLegacyEquipmentText() {
        Background bg = baseBackground("criminal", "Преступник", "Criminal");
        bg.setEquipment("2 {@item кинжала|url:dagger-phb} и 16 зм");

        JsonNode options = json(bg).get("equipmentOptions");
        assertEquals(1, options.size());
        assertEquals(
                "2 [кинжала](https://ttg.club/items/dagger-phb) и 16 зм",
                options.get(0).get("description").asText()
        );
    }

    /** Идентичность страницы-источника предыстории. */
    @Test
    void exportsSourcePageIdentity() {
        JsonNode json = json(baseBackground("sage-phb", "Мудрец", "Sage"));

        assertEquals("backgrounds", json.get("srcSection").asText());
        assertEquals("sage-phb", json.get("srcUrl").asText());
    }

    /**
     * Владение инструментами ссылками уезжает КЛЮЧАМИ справочника листа: адрес страницы
     * ({@code calligrapher-s-supplies-phb}) лист не знает и молча выбросил бы владение.
     */
    @Test
    void mapsToolReferencesToVocabularyKeys() {
        Background bg = baseBackground("acolyte", "Послушник", "Acolyte");
        bg.setToolProficiency("текст, который структура перебивает");
        bg.setToolProficiencies(List.of(
                ref("calligrapher-s-supplies-phb", "Инструменты каллиграфа"),
                ref("thieves-tools-phb", "Воровские инструменты"),
                ref("shovel-phb", "Лопата")
        ));

        JsonNode toolGrant = json(bg).get("toolGrant");
        // Лопата инструментом владения не является — её ключа у листа нет, и она отброшена
        assertEquals("[\"calligraphers-supplies\",\"thieves-tools\"]",
                toolGrant.get("items").toString());
        assertFalse(toolGrant.has("choices"));
    }

    /** Владение на выбор едет своим блоком: пул — теми же ключами вокабуляра. */
    @Test
    void mapsToolChoice() {
        Background bg = baseBackground("entertainer", "Артист", "Entertainer");
        bg.setToolChoice(new BackgroundToolChoice(1, List.of(
                ref("lute-phb", "Лютня"),
                ref("drum-phb", "Барабан")
        )));

        JsonNode choices = json(bg).get("toolGrant").get("choices");
        assertEquals(1, choices.get("count").asInt());
        assertEquals("[\"lute\",\"drum\"]", choices.get("from").toString());
    }

    /** Выбор на ноль инструментов блоком не едет: выбирать в нём нечего. */
    @Test
    void omitsEmptyToolChoice() {
        Background bg = baseBackground("hermit", "Отшельник", "Hermit");
        bg.setToolChoice(new BackgroundToolChoice(0, List.of()));

        assertFalse(json(bg).get("toolGrant").has("choices"));
    }

    /**
     * Черты на выбор уезжают идентификаторами схемы эталона — по записям справочника:
     * идентификатор собирается из английского названия, а не из слага страницы.
     */
    @Test
    void mapsFeatChoices() {
        Background bg = baseBackground("guard", "Стражник", "Guard");
        bg.setFeatChoices(List.of(
                ref("alert-phb", "Бдительный"),
                ref("skilled-phb", "Умелый"),
                ref("deleted-feat", "Удалённая черта")
        ));
        when(featRepository.findAllById(anyIterable())).thenReturn(List.of(
                feat("alert-phb", "Бдительный", "Alert"),
                feat("skilled-phb", "Умелый", "Skilled")
        ));

        JsonNode featGrant = json(bg).get("featGrant");
        // Ссылка на удалённую черту пропускается: выбор из несуществующей записи не показать
        assertEquals("[\"srd_feat_alert\",\"srd_feat_skilled\"]",
                featGrant.get("featChoices").toString());
        assertFalse(featGrant.has("featId"));
    }

    /**
     * Расширенные дары уезжают блоком {@code featData} — тем же, что у черты, а активные
     * эффекты соседним полем.
     */
    @Test
    void mapsFeatDataAndActiveEffects() {
        Background bg = baseBackground("sage", "Мудрец", "Sage");
        FeatMechanics mechanics = new FeatMechanics();
        ProficiencyGrant grant = new ProficiencyGrant();
        grant.setLanguages(Set.of(Language.DWARVISH));
        mechanics.setProficiencies(grant);
        bg.setMechanics(mechanics);

        ActiveEffect effect = new ActiveEffect();
        effect.setId("sage-insight");
        effect.setName("Учёный");
        bg.setActiveEffects(List.of(effect));

        JsonNode json = json(bg);
        assertEquals("[\"Дварфийский\"]", json.get("featData").get("languages").toString());
        assertEquals(1, json.get("activeEffects").size());
        assertEquals("Учёный", json.get("activeEffects").get(0).get("name").asText());
    }

    /** Без даров и эффектов блоков нет: пустые поля вырезаны по {@code NON_NULL}. */
    @Test
    void omitsEmptyFeatDataAndActiveEffects() {
        JsonNode json = json(baseBackground("hermit", "Отшельник", "Hermit"));

        assertFalse(json.has("featData"));
        assertFalse(json.has("activeEffects"));
    }

    private JsonNode json(Background bg) {
        return objectMapper.valueToTree(mapper.toVttg(bg));
    }

    private EntityRef ref(String url, String name) {
        return new EntityRef(url, name);
    }

    private EquipmentOption option(List<EquipmentItem> items, Integer coins) {
        return new EquipmentOption(items, coins, Coin.GC);
    }

    private EquipmentItem item(String url, String name, Integer quantity, String description) {
        return new EquipmentItem(url, name, quantity, description);
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
        bg.setSrdVersion("5.1");
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

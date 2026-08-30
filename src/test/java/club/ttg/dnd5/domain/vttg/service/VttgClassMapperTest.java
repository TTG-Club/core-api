package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.domain.character_class.model.ArmorProficiency;
import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.model.ClassFeature;
import club.ttg.dnd5.domain.character_class.model.ClassFeatureOption;
import club.ttg.dnd5.domain.character_class.model.ClassFeatureOptionsChoice;
import club.ttg.dnd5.domain.character_class.model.ClassFeatureScaling;
import club.ttg.dnd5.domain.character_class.model.ClassTableColumn;
import club.ttg.dnd5.domain.character_class.model.ClassResourceRecovery;
import club.ttg.dnd5.domain.character_class.model.ClassTableColumnPurpose;
import club.ttg.dnd5.domain.character_class.model.ClassTableItem;
import club.ttg.dnd5.domain.character_class.model.mechanics.ClassMechanics;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import club.ttg.dnd5.domain.common.dictionary.Coin;
import club.ttg.dnd5.domain.common.dictionary.Delimiter;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.model.EquipmentItem;
import club.ttg.dnd5.domain.common.model.EquipmentOption;
import club.ttg.dnd5.domain.common.dictionary.Language;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceOption;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceType;
import club.ttg.dnd5.domain.common.model.mechanics.GrantedSpellRef;
import club.ttg.dnd5.domain.common.model.mechanics.MechanicChoice;
import club.ttg.dnd5.domain.common.model.mechanics.ProficiencyGrant;
import club.ttg.dnd5.domain.common.model.mechanics.ChoiceScaling;
import club.ttg.dnd5.domain.common.model.mechanics.CounterScaling;
import club.ttg.dnd5.domain.common.model.mechanics.ResourceCounter;
import club.ttg.dnd5.domain.common.model.mechanics.ResourceRecovery;
import club.ttg.dnd5.domain.common.model.mechanics.SpellGrant;
import club.ttg.dnd5.domain.common.rest.dto.Name;
import club.ttg.dnd5.domain.character_class.model.SkillProficiency;
import club.ttg.dnd5.domain.character_class.model.WeaponProficiency;
import club.ttg.dnd5.domain.source.model.Source;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.item.repository.ItemNameRef;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VttgClassMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VttgMarkupConverter markupConverter = new VttgMarkupConverter(objectMapper);
    private final VttgClassMapper mapper = new VttgClassMapper(markupConverter,
            new VttgEquipmentMapper(markupConverter, mock(ItemRepository.class)),
            new VttgFeatMechanicsMapper(markupConverter, mock(SpellRepository.class),
                    mock(FeatRepository.class)));

    /** «Воин» — базовая механика, владения, развёртка scaling в умения, таблица и вложенный подкласс. */
    @Test
    void mapsFighterToVttgFormat() {
        CharacterClass fighter = baseClass("fighter", "Воин", "Fighter");
        fighter.setHitDice(Dice.d10);
        fighter.setArmorProficiency(new ArmorProficiency(Set.of(ArmorCategory.LIGHT, ArmorCategory.HEAVY), null));
        fighter.setWeaponProficiency(
                new WeaponProficiency(Set.of(WeaponCategory.SIMPLE_MELEE, WeaponCategory.MATERIAL_MELEE), null));
        fighter.setPrimaryCharacteristics(Set.of(Ability.STRENGTH));
        fighter.setDelimiterPrimary(Delimiter.OR);
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

        assertEquals("[\"strength\"]", json.get("primaryAbilities").toString());
        // Характеристика одна — разделителю нечего разделять, и в записи его нет.
        assertFalse(json.has("primaryAbilitiesDelimiter"));

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

    /**
     * Выбираемый список вариантов: настройка выбора уходит в {@code choiceConfig}, а
     * уровень доступа варианта — в сам вариант. Справочный список настройки не получает.
     */
    @Test
    void mapsSelectableFeatureOptions() {
        CharacterClass warlock = baseClass("warlock", "Колдун", "Warlock");
        warlock.setCasterType(CasterType.PACT);

        ClassFeature invocations = feature("eldritch-invocations", 1,
                "Таинственные воззвания", "Выберите воззвания.");
        invocations.setOptionsName("Таинственные воззвания");

        ClassFeatureOption blast = new ClassFeatureOption();
        blast.setKey("agonizing_blast");
        Name blastName = new Name();
        blastName.setName("Мучительная кара");
        blast.setName(blastName);
        blast.setRequiredClassLevel(5);
        invocations.setOptions(List.of(blast));

        ClassFeatureOptionsChoice choice = new ClassFeatureOptionsChoice();
        choice.setCount(1);
        choice.setScaling(List.of(new ChoiceScaling(1, 1), new ChoiceScaling(2, 3)));
        invocations.setOptionsChoice(choice);
        warlock.setFeatures(List.of(invocations));

        JsonNode feature = json(warlock).get("features").get(0);
        assertEquals(5, feature.get("choices").get(0).get("requiredLevel").asInt());

        JsonNode config = feature.get("choiceConfig");
        assertEquals("Таинственные воззвания", config.get("label").asText());
        assertEquals(1, config.get("count").asInt());
        assertEquals(1, config.get("progression").get("1").asInt());
        assertEquals(3, config.get("progression").get("2").asInt());
    }

    /** Справочный список вариантов остаётся справкой: настройки выбора у него нет. */
    @Test
    void skipsChoiceConfigForInformationalOptions() {
        CharacterClass wizard = baseClass("wizard", "Волшебник", "Wizard");

        ClassFeature tradition = feature("arcane-tradition", 3, "Магическая традиция", "Выберите традицию.");
        ClassFeatureOption option = new ClassFeatureOption();
        option.setKey("evocation");
        tradition.setOptions(List.of(option));
        wizard.setFeatures(List.of(tradition));

        JsonNode feature = json(wizard).get("features").get(0);
        assertNull(feature.get("choiceConfig"));
        assertNull(feature.get("choices").get(0).get("requiredLevel"));
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

    /**
     * Идентичность страницы-источника: {@code id}/{@code key} собираются из английского имени,
     * поэтому адрес страницы из них не выводится — его отдаём отдельно.
     */
    @Test
    void exportsSourcePageIdentity() {
        JsonNode json = json(baseClass("druid-phb", "Друид", "Druid"));

        assertEquals("classes", json.get("srcSection").asText());
        assertEquals("druid-phb", json.get("srcUrl").asText());
        assertEquals("druid", json.get("id").asText());
        assertEquals("druid", json.get("key").asText());
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

    /**
     * Структурированное снаряжение — основной источник: варианты получают метки «А», «Б»,
     * а предметы уезжают ссылками на карточки сайта.
     */
    @Test
    void mapsStructuredStartingEquipment() {
        CharacterClass characterClass = baseClass("fighter", "Воин", "Fighter");
        characterClass.setEquipment("Свободный текст, который не должен победить структуру");
        characterClass.setStartingEquipment(List.of(
                new EquipmentOption(List.of(
                        new EquipmentItem("chain-mail", "Кольчуга", null, null),
                        new EquipmentItem("javelin", "Метательное копьё", 8, null)
                ), 4, Coin.GC),
                new EquipmentOption(List.of(), 155, Coin.GC)
        ));

        JsonNode options = json(characterClass).get("startingEquipment");
        assertEquals(2, options.size());
        assertEquals("А", options.get(0).get("key").asText());
        assertEquals(
                "[Кольчуга](https://ttg.club/items/chain-mail), "
                        + "8 [Метательное копьё](https://ttg.club/items/javelin), 4 зм",
                options.get(0).get("description").asText()
        );
        assertEquals("Б", options.get(1).get("key").asText());
        assertEquals("155 зм", options.get(1).get("description").asText());
    }

    /**
     * Форма сайта пишет в снаряжение только ссылку на предмет, без снимка названия:
     * название подставляется из справочника, а позиция без карточки остаётся уточнением.
     * Раньше такие позиции выпадали из выгрузки и от варианта оставались одни монеты.
     */
    @Test
    void resolvesEquipmentNamesFromCatalogWhenSnapshotIsEmpty() {
        ItemRepository itemRepository = mock(ItemRepository.class);
        ItemNameRef chainMail = mock(ItemNameRef.class);
        when(chainMail.getUrl()).thenReturn("chain-mail");
        when(chainMail.getName()).thenReturn("Кольчуга");
        when(itemRepository.findNamesByUrls(Set.of("chain-mail"))).thenReturn(List.of(chainMail));
        VttgClassMapper resolvingMapper = new VttgClassMapper(markupConverter,
                new VttgEquipmentMapper(markupConverter, itemRepository),
                new VttgFeatMechanicsMapper(markupConverter, mock(SpellRepository.class),
                    mock(FeatRepository.class)));

        CharacterClass characterClass = baseClass("fighter", "Воин", "Fighter");
        characterClass.setStartingEquipment(List.of(
                new EquipmentOption(List.of(
                        new EquipmentItem("chain-mail", null, 1, null),
                        new EquipmentItem(null, null, 1, "музыкальный инструмент")
                ), 4, Coin.GC)
        ));

        JsonNode option = objectMapper.valueToTree(resolvingMapper.toVttg(characterClass))
                .get("startingEquipment").get(0);
        assertEquals(
                "[Кольчуга](https://ttg.club/items/chain-mail), музыкальный инструмент, 4 зм",
                option.get("description").asText()
        );
        JsonNode items = option.get("items");
        assertEquals(1, items.size());
        assertEquals("chain-mail", items.get(0).get("url").asText());
        assertEquals("Кольчуга", items.get(0).get("name").asText());
    }

    /** Без структуры остаётся легаси-текст одним вариантом — с разобранной разметкой. */
    @Test
    void fallsBackToLegacyEquipmentText() {
        CharacterClass characterClass = baseClass("fighter", "Воин", "Fighter");
        characterClass.setEquipment("{@item Кольчуга|url:chain-mail} и 4 зм");

        JsonNode options = json(characterClass).get("startingEquipment");
        assertEquals(1, options.size());
        assertEquals("А", options.get(0).get("key").asText());
        assertEquals(
                "[Кольчуга](https://ttg.club/items/chain-mail) и 4 зм",
                options.get(0).get("description").asText()
        );
    }


    /**
     * Заклинательная конфигурация берётся из записи, а не из карты канонических классов:
     * у самописного класса в карте его нет, и до появления полей вся конфигурация молча
     * пропадала.
     */
    @Test
    void takesSpellcastingFromRecord() {
        CharacterClass homebrew = baseClass("mystic", "Мистик", "Mystic");
        homebrew.setCasterType(CasterType.HALF);
        homebrew.setSpellcastingAbility(Ability.INTELLIGENCE);
        homebrew.setSpellcastingStartLevel(3);
        homebrew.setSubclassLabel("Мистический орден");
        homebrew.setSubclassLevel(2);

        JsonNode json = json(homebrew);
        assertEquals("half", json.get("spellcasting").get("type").asText());
        assertEquals("intelligence", json.get("spellcasting").get("ability").asText());
        assertEquals(3, json.get("spellcasting").get("startLevel").asInt());
        assertEquals("Мистический орден", json.get("subclassLabel").asText());
        assertEquals(2, json.get("subclassLevel").asInt());
    }

    /** У записи без своих полей остаётся прежнее поведение — каноническая карта по ключу. */
    @Test
    void fallsBackToCanonicalSpellcasting() {
        CharacterClass wizard = baseClass("wizard", "Волшебник", "Wizard");
        wizard.setCasterType(CasterType.FULL);

        JsonNode json = json(wizard);
        assertEquals("intelligence", json.get("spellcasting").get("ability").asText());
        assertEquals(1, json.get("spellcasting").get("startLevel").asInt());
        assertEquals("Магическая традиция", json.get("subclassLabel").asText());
    }

    /** Эффекты класса и его умений уезжают как есть — той же моделью, что у черты. */
    @Test
    void exportsActiveEffects() {
        CharacterClass barbarian = baseClass("barbarian", "Варвар", "Barbarian");
        barbarian.setActiveEffects(List.of(effect("unarmored-defense", "Защита без доспехов")));

        ClassFeature rage = feature("rage", 1, "Ярость", "Вы впадаете в ярость.");
        rage.setActiveEffects(List.of(effect("rage", "Ярость")));
        barbarian.setFeatures(List.of(rage));

        JsonNode json = json(barbarian);
        assertEquals(1, json.get("activeEffects").size());
        assertEquals("Защита без доспехов", json.get("activeEffects").get(0).get("name").asText());
        assertEquals("Ярость", json.get("features").get(0).get("activeEffects").get(0).get("name").asText());
    }

    /** Заклинания, выданные умением, уезжают списком id — как их ждёт эталон. */
    @Test
    void exportsGrantedSpellsOfFeature() {
        CharacterClass ranger = baseClass("ranger", "Следопыт", "Ranger");
        ClassFeature favoredEnemy = feature("favored-enemy", 1, "Избранный враг", "Вы знаете «Метку охотника».");

        GrantedSpellRef reference = new GrantedSpellRef();
        reference.setUrl("hunters-mark");
        reference.setName("Метка охотника");
        SpellGrant grant = new SpellGrant();
        grant.setSpells(List.of(reference));
        ClassMechanics mechanics = new ClassMechanics();
        mechanics.setSpells(grant);
        favoredEnemy.setMechanics(mechanics);

        ranger.setFeatures(List.of(favoredEnemy));

        JsonNode feature = json(ranger).get("features").get(0);
        assertEquals("[\"hunters-mark\"]", feature.get("grantedSpells").toString());
    }

    /**
     * Колонка «известных заговоров» хранит итог, а мастер повышения уровня спрашивает
     * прирост: он и считается разностью с предыдущим заполненным уровнем.
     */
    @Test
    void derivesNewCantripsFromColumnPurpose() {
        CharacterClass wizard = baseClass("wizard", "Волшебник", "Wizard");
        ClassTableColumn cantrips = new ClassTableColumn("Известные заговоры",
                List.of(new ClassTableItem(1, "3"), new ClassTableItem(4, "4"), new ClassTableItem(10, "5")));
        cantrips.setPurpose(ClassTableColumnPurpose.CANTRIPS_KNOWN);
        wizard.setTable(List.of(cantrips));

        JsonNode levels = json(wizard).get("levelTable");
        assertEquals(3, levels.get(0).get("newCantrips").asInt());
        assertFalse(levels.get(1).has("newCantrips"));
        assertEquals(1, levels.get(3).get("newCantrips").asInt());
        assertEquals(1, levels.get(9).get("newCantrips").asInt());
    }

    /** Ресурс с формульным максимумом приходит из механики: колонки таблицы у него нет. */
    @Test
    void exportsFormulaCounterFromMechanics() {
        CharacterClass fighter = baseClass("fighter", "Воин", "Fighter");
        ClassFeature secondWind = feature("second-wind", 1, "Второе дыхание", "Восстановите хиты.");

        ResourceCounter counter = new ResourceCounter();
        counter.setKey("second-wind");
        counter.setName("Второе дыхание");
        counter.setShortName("Дыхание");
        counter.setMax("@prof");
        counter.setRecovery(ResourceRecovery.LONG_REST);
        ClassMechanics mechanics = new ClassMechanics();
        mechanics.setCounters(List.of(counter));
        secondWind.setMechanics(mechanics);

        fighter.setFeatures(List.of(secondWind));

        JsonNode counters = json(fighter).get("counters");
        assertEquals(1, counters.size());
        assertEquals("second-wind", counters.get(0).get("key").asText());
        assertEquals("Дыхание", counters.get(0).get("shortName").asText());
        assertEquals("@prof", counters.get(0).get("formula").asText());
        assertEquals("long", counters.get(0).get("recovery").asText());
    }

    /**
     * Ресурс со ступенями по уровням уезжает прогрессией: формулой ряд «4 с третьего,
     * 5 с седьмого, 6 с пятнадцатого» не пишется, а у потребителя это тот же вид записи,
     * что и у колонки таблицы.
     */
    @Test
    void exportsScaledCounterFromMechanics() {
        CharacterClass fighter = baseClass("fighter", "Воин", "Fighter");
        ClassFeature superiority = feature("combat-superiority", 3, "Боевое превосходство",
                "Кости превосходства.");

        ResourceCounter counter = new ResourceCounter();
        counter.setKey("superiority-dice");
        counter.setName("Кости превосходства");
        counter.setRecovery(ResourceRecovery.SHORT_REST);
        counter.setScaling(List.of(
                new CounterScaling(7, 5),
                new CounterScaling(3, 4),
                new CounterScaling(15, 6)));
        ClassMechanics mechanics = new ClassMechanics();
        mechanics.setCounters(List.of(counter));
        superiority.setMechanics(mechanics);

        fighter.setFeatures(List.of(superiority));

        JsonNode counters = json(fighter).get("counters");
        assertEquals(1, counters.size());

        JsonNode exported = counters.get(0);
        assertEquals("superiority-dice", exported.get("key").asText());
        assertEquals("short", exported.get("recovery").asText());
        // Счётчик появляется с первой ступени: до третьего уровня ресурса нет вовсе
        assertEquals(3, exported.get("startLevel").asInt());
        assertFalse(exported.has("formula"));

        JsonNode progression = exported.get("progression");
        assertEquals(4, progression.get("3").asInt());
        assertEquals(5, progression.get("7").asInt());
        assertEquals(6, progression.get("15").asInt());
    }

    /**
     * «Второе дыхание» 2024 года: один заряд возвращает короткий отдых, все —
     * продолжительный. Такой откат — своё значение словаря, а не короткий отдых: тот
     * возвращает ресурс целиком.
     */
    @Test
    void exportsShortRestOneRecovery() {
        CharacterClass fighter = baseClass("fighter", "Воин", "Fighter");
        ClassFeature secondWind = feature("second-wind", 1, "Второе дыхание", "Восстановите хиты.");

        ResourceCounter counter = new ResourceCounter();
        counter.setKey("second-wind");
        counter.setName("Второе дыхание");
        counter.setMax("2");
        counter.setRecovery(ResourceRecovery.SHORT_REST_ONE);
        ClassMechanics mechanics = new ClassMechanics();
        mechanics.setCounters(List.of(counter));
        secondWind.setMechanics(mechanics);

        fighter.setFeatures(List.of(secondWind));

        assertEquals("short-one", json(fighter).get("counters").get(0).get("recovery").asText());
    }

    /**
     * Нижняя граница максимума уезжает вместе с формулой: вдохновение барда равно
     * модификатору Харизмы, но с Харизмой +0 бард всё равно вдохновляет один раз.
     */
    @Test
    void exportsCounterMinimum() {
        CharacterClass bard = baseClass("bard", "Бард", "Bard");
        ClassFeature inspiration = feature("bardic-inspiration", 1, "Вдохновение барда",
                "Дайте кость вдохновения.");

        ResourceCounter counter = new ResourceCounter();
        counter.setKey("bardic-inspiration");
        counter.setName("Вдохновение барда");
        counter.setMax("@mod.cha");
        counter.setMin(1);
        counter.setRecovery(ResourceRecovery.SHORT_REST_ONE);
        ClassMechanics mechanics = new ClassMechanics();
        mechanics.setCounters(List.of(counter));
        inspiration.setMechanics(mechanics);

        bard.setFeatures(List.of(inspiration));

        JsonNode exported = json(bard).get("counters").get(0);
        assertEquals("@mod.cha", exported.get("formula").asText());
        assertEquals(1, exported.get("min").asInt());
    }

    /**
     * Ресурс, записанный и колонкой, и механикой, уезжает один раз — механикой: колонка
     * осталась от прежней записи и не знает ни о нижней границе, ни о порции короткого
     * отдыха.
     */
    @Test
    void mechanicsCounterWinsOverColumnWithSameKey() {
        CharacterClass fighter = baseClass("fighter", "Воин", "Fighter");
        ClassTableColumn column = new ClassTableColumn("Второе дыхание",
                List.of(new ClassTableItem(1, "2")));
        column.setKey("second-wind");
        column.setResourceRecovery(ClassResourceRecovery.LONG_REST);
        fighter.setTable(List.of(column));

        ClassFeature secondWind = feature("second-wind", 1, "Второе дыхание", "Восстановите хиты.");
        ResourceCounter counter = new ResourceCounter();
        counter.setKey("second-wind");
        counter.setName("Второе дыхание");
        counter.setMax("@prof");
        counter.setRecovery(ResourceRecovery.SHORT_REST_ONE);
        ClassMechanics mechanics = new ClassMechanics();
        mechanics.setCounters(List.of(counter));
        secondWind.setMechanics(mechanics);
        fighter.setFeatures(List.of(secondWind));

        JsonNode counters = json(fighter).get("counters");
        assertEquals(1, counters.size());
        assertEquals("short-one", counters.get(0).get("recovery").asText());
        assertEquals("@prof", counters.get(0).get("formula").asText());
        // Колонка остаётся колонкой таблицы: счётчиком она быть перестала, а прогрессию
        // в книге показывает по-прежнему
        assertEquals("second-wind", json(fighter).get("tableColumns").get(0).get("key").asText());
    }

    /**
     * Ресурс, отмеченный «показывать в таблице», едет и счётчиком, и колонкой: ряд по
     * уровням у него уже задан ступенями, и второй раз колонкой его не набирают.
     */
    @Test
    void exportsCounterAsTableColumn() {
        CharacterClass barbarian = baseClass("barbarian", "Варвар", "Barbarian");
        ClassFeature rage = feature("rage", 1, "Ярость", "Впадите в ярость.");

        ResourceCounter counter = new ResourceCounter();
        counter.setKey("rages");
        counter.setName("Ярости");
        counter.setRecovery(ResourceRecovery.LONG_REST);
        counter.setShowInTable(true);
        counter.setScaling(List.of(new CounterScaling(1, 2), new CounterScaling(3, 3)));
        ClassMechanics mechanics = new ClassMechanics();
        mechanics.setCounters(List.of(counter));
        rage.setMechanics(mechanics);

        barbarian.setFeatures(List.of(rage));

        JsonNode json = json(barbarian);
        assertEquals("rages", json.get("tableColumns").get(0).get("key").asText());
        assertEquals("Ярости", json.get("tableColumns").get(0).get("label").asText());

        JsonNode levels = json.get("levelTable");
        assertEquals(2, levels.get(0).get("rages").asInt());
        assertEquals(3, levels.get(2).get("rages").asInt());
    }

    /**
     * Ряд формулы считается от уровня: применений «Второго дыхания» столько же, сколько
     * бонус мастерства, и колонка книги повторяет его ступени.
     */
    @Test
    void exportsFormulaCounterAsTableColumn() {
        CharacterClass fighter = baseClass("fighter", "Воин", "Fighter");
        ClassFeature secondWind = feature("second-wind", 1, "Второе дыхание", "Восстановите хиты.");

        ResourceCounter counter = new ResourceCounter();
        counter.setKey("second-wind");
        counter.setName("Второе дыхание");
        counter.setMax("@prof");
        counter.setRecovery(ResourceRecovery.SHORT_REST_ONE);
        counter.setShowInTable(true);
        ClassMechanics mechanics = new ClassMechanics();
        mechanics.setCounters(List.of(counter));
        secondWind.setMechanics(mechanics);

        fighter.setFeatures(List.of(secondWind));

        JsonNode levels = json(fighter).get("levelTable");
        assertEquals(2, levels.get(0).get("second-wind").asInt());
        assertEquals(3, levels.get(4).get("second-wind").asInt());
        assertEquals(6, levels.get(19).get("second-wind").asInt());
    }

    /**
     * Ресурс умения появляется в таблице с уровня самого умения: до него у персонажа
     * ресурса нет, и «0 из 0» в книге только мешал бы.
     */
    @Test
    void countersColumnStartsAtFeatureLevel() {
        CharacterClass monk = baseClass("monk", "Монах", "Monk");
        ClassFeature ki = feature("ki", 2, "Ки", "Очки ки.");

        ResourceCounter counter = new ResourceCounter();
        counter.setKey("ki-points");
        counter.setName("Очки ки");
        counter.setMax("@level");
        counter.setRecovery(ResourceRecovery.SHORT_REST);
        counter.setShowInTable(true);
        ClassMechanics mechanics = new ClassMechanics();
        mechanics.setCounters(List.of(counter));
        ki.setMechanics(mechanics);

        monk.setFeatures(List.of(ki));

        JsonNode levels = json(monk).get("levelTable");
        assertFalse(levels.get(0).has("ki-points"));
        assertEquals(2, levels.get(1).get("ki-points").asInt());
    }

    /**
     * Максимум по модификатору характеристики одинакового ряда для всех не имеет:
     * колонкой такой ресурс не показывается, даже когда галочка стоит.
     */
    @Test
    void skipsTableColumnForAbilityFormula() {
        CharacterClass bard = baseClass("bard", "Бард", "Bard");
        ClassFeature inspiration = feature("bardic-inspiration", 1, "Вдохновение барда", "Кость.");

        ResourceCounter counter = new ResourceCounter();
        counter.setKey("bardic-inspiration");
        counter.setName("Вдохновение барда");
        counter.setMax("@mod.cha");
        counter.setMin(1);
        counter.setRecovery(ResourceRecovery.SHORT_REST_ONE);
        counter.setShowInTable(true);
        ClassMechanics mechanics = new ClassMechanics();
        mechanics.setCounters(List.of(counter));
        inspiration.setMechanics(mechanics);

        bard.setFeatures(List.of(inspiration));

        assertFalse(json(bard).has("tableColumns"));
    }

    /**
     * Выбор со ступенями количества показывается колонкой: оружейных приёмов у воина три
     * с первого уровня, четыре с четвёртого. Ряд собирается из ступеней, колонкой его
     * второй раз не набирают.
     */
    @Test
    void exportsChoiceScalingAsTableColumn() {
        CharacterClass fighter = baseClass("fighter", "Воин", "Fighter");
        ClassFeature masteries = feature("weapon-masteries", 1, "Оружейные приёмы", "Приёмы.");

        MechanicChoice choice = new MechanicChoice();
        choice.setKey("weapon-mastery");
        choice.setType(ChoiceType.WEAPON_MASTERY);
        choice.setLabel("Выберите оружейный приём");
        choice.setShortName("Приёмы");
        choice.setShowInTable(true);
        choice.setScaling(List.of(new ChoiceScaling(1, 3), new ChoiceScaling(4, 4)));
        ClassMechanics mechanics = new ClassMechanics();
        mechanics.setChoices(List.of(choice));
        masteries.setMechanics(mechanics);

        fighter.setFeatures(List.of(masteries));

        JsonNode json = json(fighter);
        assertEquals("weapon-mastery", json.get("tableColumns").get(0).get("key").asText());
        assertEquals("Приёмы", json.get("tableColumns").get(0).get("label").asText());

        JsonNode levels = json.get("levelTable");
        assertEquals(3, levels.get(0).get("weapon-mastery").asInt());
        assertEquals(4, levels.get(3).get("weapon-mastery").asInt());

        JsonNode exported = json.get("features").get(0).get("featData").get("choices").get(0);
        assertEquals(3, exported.get("scaling").get("1").asInt());
        assertEquals(4, exported.get("scaling").get("4").asInt());
    }

    /**
     * Колонка с той же подписью не задваивается, даже когда ключа у неё нет: у подкласса
     * лежит своя копия родительской таблицы, и «Ярость» рисовалась бы дважды — своя и
     * выведенная из ресурса.
     */
    @Test
    void skipsDerivedColumnWithTakenLabel() {
        CharacterClass barbarian = baseClass("barbarian", "Варвар", "Barbarian");
        ClassTableColumn copied = new ClassTableColumn("Ярость",
                List.of(new ClassTableItem(1, "2"), new ClassTableItem(3, "3")));
        barbarian.setTable(List.of(copied));

        ClassFeature rage = feature("rage", 1, "Ярость", "Впадите в ярость.");
        ResourceCounter counter = new ResourceCounter();
        counter.setKey("rages");
        counter.setName("Ярость");
        counter.setRecovery(ResourceRecovery.LONG_REST);
        counter.setShowInTable(true);
        counter.setScaling(List.of(new CounterScaling(1, 2), new CounterScaling(3, 3)));
        ClassMechanics mechanics = new ClassMechanics();
        mechanics.setCounters(List.of(counter));
        rage.setMechanics(mechanics);
        barbarian.setFeatures(List.of(rage));

        JsonNode columns = json(barbarian).get("tableColumns");
        assertEquals(1, columns.size());
        assertEquals("Ярость", columns.get(0).get("label").asText());
    }

    /**
     * Свой ключ колонки важнее выведенного из подписи: по нему лист хранит потраченный
     * остаток, и перевод подписи не должен обнулять счётчики.
     */
    @Test
    void prefersExplicitColumnKey() {
        CharacterClass barbarian = baseClass("barbarian", "Варвар", "Barbarian");
        ClassTableColumn rages = new ClassTableColumn("Ярости",
                List.of(new ClassTableItem(1, "2"), new ClassTableItem(3, "3")));
        rages.setKey("rages");
        rages.setResourceRecovery(ClassResourceRecovery.LONG_REST);
        barbarian.setTable(List.of(rages));

        JsonNode json = json(barbarian);
        assertEquals("rages", json.get("counters").get(0).get("key").asText());
        assertEquals("rages", json.get("tableColumns").get(0).get("key").asText());
        assertEquals("2", json.get("levelTable").get(0).get("rages").asText());
    }


    /**
     * Дары умения уезжают тем же блоком, что у черты и предыстории: у потребителя их
     * применяет один и тот же код, и вторая форма означала бы второй разбор.
     */
    @Test
    void exportsFeatureGrantsAsFeatData() {
        CharacterClass rogue = baseClass("rogue", "Плут", "Rogue");
        ClassFeature expertise = feature("expertise", 1, "Экспертиза", "Удвойте бонус мастерства.");

        ProficiencyGrant granted = new ProficiencyGrant();

        granted.setLanguages(Set.of(Language.COMMON));

        MechanicChoice choice = new MechanicChoice();

        choice.setKey("skill");
        choice.setType(ChoiceType.SKILL);
        choice.setCount(2);
        choice.setOptions(List.of(new ChoiceOption("STEALTH", "Скрытность")));

        ClassMechanics mechanics = new ClassMechanics();

        mechanics.setProficiencies(granted);
        mechanics.setChoices(List.of(choice));
        expertise.setMechanics(mechanics);

        rogue.setFeatures(List.of(expertise));

        JsonNode featData = json(rogue).get("features").get(0).get("featData");
        assertEquals("feat", featData.get("type").asText());
        assertEquals("[\"Общий\"]", featData.get("languages").toString());
        assertEquals("skill", featData.get("choices").get(0).get("type").asText());
        assertEquals(2, featData.get("choices").get(0).get("count").asInt());
    }

    private ActiveEffect effect(String id, String name) {
        ActiveEffect effect = new ActiveEffect();
        effect.setId(id);
        effect.setName(name);
        return effect;
    }

    /** Основные характеристики: список из двух выводится вместе с разделителем. */
    @Test
    void mapsPrimaryAbilitiesWithDelimiter() {
        CharacterClass monk = baseClass("monk", "Монах", "Monk");
        monk.setPrimaryCharacteristics(Set.of(Ability.DEXTERITY, Ability.WISDOM));
        monk.setDelimiterPrimary(Delimiter.AND);
        monk.setCasterType(CasterType.NONE);

        JsonNode json = json(monk);
        assertEquals("[\"dexterity\",\"wisdom\"]", sortedInsensitive(json.get("primaryAbilities")));
        assertEquals("and", json.get("primaryAbilitiesDelimiter").asText());
    }

    /** Характеристики не заполнены — поля в записи не появляются пустыми. */
    @Test
    void omitsPrimaryAbilitiesWhenEmpty() {
        CharacterClass rogue = baseClass("rogue", "Плут", "Rogue");
        rogue.setCasterType(CasterType.NONE);

        JsonNode json = json(rogue);
        assertFalse(json.has("primaryAbilities"));
        assertFalse(json.has("primaryAbilitiesDelimiter"));
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

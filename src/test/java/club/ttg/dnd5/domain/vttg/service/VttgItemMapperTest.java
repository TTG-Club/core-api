package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.model.Roll;
import club.ttg.dnd5.domain.item.model.Armor;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.model.ItemType;
import club.ttg.dnd5.domain.item.model.tool.Tool;
import club.ttg.dnd5.domain.item.model.weapon.AmmunitionType;
import club.ttg.dnd5.domain.item.model.weapon.Damage;
import club.ttg.dnd5.domain.item.model.weapon.DamagePart;
import club.ttg.dnd5.domain.item.model.weapon.Mastery;
import club.ttg.dnd5.domain.item.model.weapon.Property;
import club.ttg.dnd5.domain.item.model.weapon.Weapon;
import club.ttg.dnd5.domain.item.rest.dto.Range;
import club.ttg.dnd5.domain.source.model.Source;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VttgItemMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VttgItemMapper mapper = new VttgItemMapper(new VttgMarkupConverter(objectMapper));

    /** Воинское рукопашное оружие → раздел weapons с боевыми полями (как в эталоне). */
    @Test
    void mapsMartialMeleeWeapon() {
        Item item = baseItem("longsword", "Длинный меч", "Longsword");
        Weapon weapon = new Weapon();
        weapon.setCategory(WeaponCategory.MATERIAL_MELEE);
        weapon.setDamage(damage(Dice.d8, DamageType.SLASHING));
        weapon.setProperties(Set.of(Property.VERSATILE));
        weapon.setVersatile(roll(Dice.d10));
        weapon.setMastery(Mastery.SAP);
        item.setWeapon(weapon);
        item.setTypes(Set.of(ItemType.MARTIAL_WEAPON, ItemType.MELEE_WEAPON));

        JsonNode json = json(item);
        assertEquals("longsword-phb", json.get("id").asText());
        assertEquals("weapon", json.get("type").asText());
        assertEquals("weapons", json.get("section").asText());
        assertEquals("longsword", json.get("baseType").asText());
        assertEquals("martial", json.get("weaponCategory").asText());
        assertEquals("melee", json.get("rangeType").asText());
        JsonNode damagePart = json.get("damageParts").get(0);
        assertEquals("1к8", damagePart.get("formula").asText());
        assertEquals("slashing", damagePart.get("type").asText());
        assertEquals("1к10", damagePart.get("versatileFormula").asText());
        assertEquals(5, json.get("reach").asInt());
        assertEquals("sap", json.get("mastery").asText());
        assertEquals("auto", json.get("proficiencyMode").asText());
        assertEquals("Оружие", json.get("typeLabel").asText());
        assertFalse(json.get("isMagical").asBoolean());
        assertTrue(json.get("isSRD").asBoolean());
    }

    /** Дальнобойное оружие с боеприпасами: range {normal,long}, ammunitionType, без reach. */
    @Test
    void mapsRangedWeaponWithAmmunition() {
        Item item = baseItem("shortbow", "Короткий лук", "Shortbow");
        Weapon weapon = new Weapon();
        weapon.setCategory(WeaponCategory.SIMPLE_RANGED);
        weapon.setDamage(damage(Dice.d6, DamageType.PIERCING));
        weapon.setProperties(Set.of(Property.AMMUNITION, Property.TWO_HANDED));
        weapon.setAmmo(AmmunitionType.ARROW);
        Range range = new Range();
        range.setNormal((short) 80);
        range.setMax((short) 320);
        weapon.setRange(range);
        item.setWeapon(weapon);
        item.setTypes(Set.of(ItemType.SIMPLE_WEAPON, ItemType.RANGED_WEAPON));

        JsonNode json = json(item);
        assertEquals("ranged", json.get("rangeType").asText());
        assertEquals("arrows", json.get("ammunitionType").asText());
        assertEquals(80, json.get("range").get("normal").asInt());
        assertEquals(320, json.get("range").get("long").asInt());
        assertFalse(json.has("reach"));
        assertEquals("[\"ammunition\",\"two-handed\"]", json.get("weaponProperties").toString());
    }

    /** Доспех → раздел armor с доспешными полями; maxDexBonus присутствует даже как null. */
    @Test
    void mapsArmorWithExplicitNullMaxDex() {
        Item item = baseItem("padded", "Стёганый доспех", "Padded");
        Armor armor = new Armor();
        armor.setCategory(ArmorCategory.LIGHT);
        armor.setArmorClass(11);
        armor.setMod(Armor.DexterityMod.PLUS);
        armor.setStealth(true);
        item.setArmor(armor);
        item.setTypes(Set.of(ItemType.LIGHT_ARMOR));

        JsonNode json = json(item);
        assertEquals("equipment", json.get("type").asText());
        assertEquals("armor", json.get("section").asText());
        assertEquals("light", json.get("equipmentCategory").asText());
        assertEquals(11, json.get("baseArmorAC").asInt());
        assertTrue(json.get("stealthDisadvantage").asBoolean());
        assertEquals(0, json.get("strengthRequirement").asInt());
        assertTrue(json.has("maxDexBonus"));
        assertTrue(json.get("maxDexBonus").isNull());
    }

    /** Средний доспех с требованием Силы и пределом Ловкости. */
    @Test
    void mapsMediumArmorWithStrengthAndDexCap() {
        Item item = baseItem("chain-mail", "Кольчуга", "Chain Mail");
        Armor armor = new Armor();
        armor.setCategory(ArmorCategory.HEAVY);
        armor.setArmorClass(16);
        armor.setMod(Armor.DexterityMod.NONE);
        armor.setStealth(true);
        armor.setStrength("13");
        item.setArmor(armor);
        item.setTypes(Set.of(ItemType.HEAVY_ARMOR));

        JsonNode json = json(item);
        assertEquals("heavy", json.get("equipmentCategory").asText());
        assertEquals(0, json.get("maxDexBonus").asInt());
        assertEquals(13, json.get("strengthRequirement").asInt());
    }

    /** Прочее снаряжение без weapon/armor → раздел trinkets (бывш. gear), type equipment. */
    @Test
    void mapsPlainGear() {
        Item item = baseItem("backpack", "Рюкзак", "Backpack");
        item.setTypes(Set.of(ItemType.ADVENTURING_GEAR));
        item.setCost("2");

        JsonNode json = json(item);
        assertEquals("equipment", json.get("type").asText());
        assertEquals("trinkets", json.get("section").asText());
        assertEquals("adventurer-equipment", json.get("equipmentCategory").asText());
        assertFalse(json.has("baseArmorAC"));
        assertFalse(json.has("weaponCategory"));
    }

    /** Инструмент (категория TOOL) → раздел tools, type tool. */
    @Test
    void mapsTool() {
        Item item = baseItem("thieves-tools", "Воровские инструменты", "Thieves' Tools");
        item.setTypes(Set.of(ItemType.TOOL));

        JsonNode json = json(item);
        assertEquals("tool", json.get("type").asText());
        assertEquals("tools", json.get("section").asText());
        assertEquals("other", json.get("toolCategory").asText());
    }

    @Test
    void mapsWeaponByItemTypeWhenArmorDataExists() {
        Item item = baseItem("spear", "Spear", "Spear");
        item.setTypes(Set.of(ItemType.SIMPLE_WEAPON, ItemType.MELEE_WEAPON));
        Weapon weapon = new Weapon();
        weapon.setCategory(WeaponCategory.SIMPLE_MELEE);
        weapon.setDamage(damage(Dice.d6, DamageType.PIERCING));
        weapon.setProperties(Set.of(Property.THROWN));
        weapon.setMastery(Mastery.SAP);
        item.setWeapon(weapon);
        Armor armor = new Armor();
        armor.setCategory(ArmorCategory.LIGHT);
        armor.setArmorClass(11);
        item.setArmor(armor);

        JsonNode json = json(item);
        assertEquals("weapon", json.get("type").asText());
        assertEquals("weapons", json.get("section").asText());
        assertEquals("1к6", json.get("damageParts").get(0).get("formula").asText());
        assertEquals("[\"thrown\"]", json.get("weaponProperties").toString());
        assertEquals(5, json.get("reach").asInt());
        assertEquals("sap", json.get("mastery").asText());
        assertFalse(json.has("baseArmorAC"));
        assertFalse(json.has("equipmentCategory"));
    }

    @Test
    void mapsArmorByItemTypeWhenWeaponDataExists() {
        Item item = baseItem("shield", "Shield", "Shield");
        item.setTypes(Set.of(ItemType.SHIELD));
        Armor armor = new Armor();
        armor.setCategory(ArmorCategory.SHIELD);
        armor.setArmorClass(2);
        armor.setStealth(false);
        item.setArmor(armor);
        Weapon weapon = new Weapon();
        weapon.setCategory(WeaponCategory.SIMPLE_MELEE);
        weapon.setDamage(damage(Dice.d4, DamageType.BLUDGEONING));
        weapon.setProperties(Set.of(Property.LIGHT));
        item.setWeapon(weapon);

        JsonNode json = json(item);
        assertEquals("equipment", json.get("type").asText());
        assertEquals("armor", json.get("section").asText());
        assertEquals("shield", json.get("equipmentCategory").asText());
        assertEquals(2, json.get("baseArmorAC").asInt());
        assertFalse(json.get("stealthDisadvantage").asBoolean());
        assertFalse(json.has("damageParts"));
        assertFalse(json.has("weaponProperties"));
        assertFalse(json.has("reach"));
        assertFalse(json.has("mastery"));
    }

    /**
     * Идентичность страницы-источника: раздел сайта — {@code items}, тогда как лист компендиума
     * зависит от вида предмета, а {@code id} несёт суффикс источника. Ссылку из описания можно
     * разрешить только по этой паре.
     */
    @Test
    void exportsSourcePageIdentity() {
        JsonNode json = json(baseItem("longsword", "Длинный меч", "Longsword"));

        assertEquals("items", json.get("srcSection").asText());
        assertEquals("longsword", json.get("srcUrl").asText());
        assertEquals("longsword-phb", json.get("id").asText());
    }

    /**
     * Урон, заданный частями-формулами, перебивает прежнюю связку «кости + тип»: форма
     * пишет обе, и без приоритета оружие уехало бы с уроном, который автор уже поправил.
     */
    @Test
    void prefersStoredDamagePartsOverLegacyDice() {
        Item item = baseItem("flame-tongue", "Огненный язык", "Flame Tongue");
        Weapon weapon = new Weapon();
        weapon.setCategory(WeaponCategory.MATERIAL_MELEE);
        weapon.setDamage(damage(Dice.d8, DamageType.SLASHING));
        weapon.setVersatile(roll(Dice.d10));
        weapon.setProperties(Set.of(Property.VERSATILE));
        weapon.setDamageParts(List.of(
                damagePart("1к8@dmg.slashing", "1к10@dmg.slashing"),
                damagePart("2к6@dmg.fire", null)));
        item.setWeapon(weapon);
        item.setTypes(Set.of(ItemType.MARTIAL_WEAPON, ItemType.MELEE_WEAPON));

        JsonNode parts = json(item).get("damageParts");
        assertEquals(2, parts.size());
        assertEquals("1к8@dmg.slashing", parts.get(0).get("formula").asText());
        assertEquals("1к10@dmg.slashing", parts.get(0).get("versatileFormula").asText());
        assertEquals("2к6@dmg.fire", parts.get(1).get("formula").asText());
        assertFalse(parts.get(1).has("versatileFormula"));
        // Тип части живёт токеном формулы — отдельного поля у неё нет
        assertFalse(parts.get(0).has("type"));
    }

    /** Боевые поля, которых у справочника раньше не было, уезжают как заданы. */
    @Test
    void exportsWeaponAttackAndSaveFields() {
        Item item = baseItem("net", "Сеть", "Net");
        Weapon weapon = new Weapon();
        weapon.setCategory(WeaponCategory.MATERIAL_MELEE);
        weapon.setDamage(damage(Dice.d4, DamageType.BLUDGEONING));
        weapon.setAttackAbility("dexterity");
        weapon.setProficiencyMode("always");
        weapon.setAttackBonus(1);
        weapon.setDamageAbility("none");
        weapon.setDamageBonus(2);
        weapon.setSaveType("strength");
        weapon.setSaveEffect("none");
        weapon.setReach(10);
        item.setWeapon(weapon);
        item.setTypes(Set.of(ItemType.MARTIAL_WEAPON, ItemType.MELEE_WEAPON));

        JsonNode json = json(item);
        assertEquals("dexterity", json.get("attackAbility").asText());
        assertEquals("always", json.get("proficiencyMode").asText());
        assertEquals(1, json.get("attackBonus").asInt());
        assertEquals("none", json.get("damageAbility").asText());
        assertEquals(2, json.get("damageBonus").asInt());
        assertEquals("strength", json.get("saveType").asText());
        assertEquals("none", json.get("saveEffect").asText());
        // Заданная досягаемость перебивает вывод по свойству «Досягаемость»
        assertEquals(10, json.get("reach").asInt());
    }

    /**
     * Базовый вид оружия выводится по адресу страницы, а не по английскому названию:
     * в книгах оно бывает записано с уточнением («Sword, Long»), и слаг с ключом листа
     * тогда не сходится.
     */
    @Test
    void derivesWeaponBaseTypeFromUrl() {
        Item item = baseItem("longsword", "Длинный меч", "Sword, Long");
        item.setTypes(Set.of(ItemType.MARTIAL_WEAPON));
        item.setWeapon(new Weapon());

        assertEquals("longsword", json(item).get("baseType").asText());
    }

    /** Заданный в справочнике базовый вид перебивает любой вывод. */
    @Test
    void prefersStoredWeaponBaseType() {
        Item item = baseItem("storm-blade", "Клинок бури", "Storm Blade");
        item.setTypes(Set.of(ItemType.MARTIAL_WEAPON));
        Weapon weapon = new Weapon();
        weapon.setBaseType("longsword");
        item.setWeapon(weapon);

        assertEquals("longsword", json(item).get("baseType").asText());
    }

    /**
     * Ключ инструмента — по адресу страницы: слаг английского названия даёт
     * «calligrapher-s-supplies», а лист знает «calligraphers-supplies» и незнакомое
     * владение молча выбрасывает.
     */
    @Test
    void derivesToolBaseTypeFromUrl() {
        Item item = baseItem("calligrapher-s-supplies", "Инструменты каллиграфа",
                "Calligrapher's Supplies");
        item.setTypes(Set.of(ItemType.TOOL, ItemType.ARTISAN_S_TOOLS));

        JsonNode json = json(item);
        assertEquals("calligraphers-supplies", json.get("baseToolType").asText());
        assertEquals("artisan", json.get("toolCategory").asText());
    }

    /**
     * Игровой набор и инструменты ремесленника — тоже инструменты, хотя их типы заведены
     * категорией {@code ITEM}: без этого «Набор игральных карт» уезжал безделушкой, и
     * владение им лист не засчитывал.
     */
    @Test
    void mapsGamingSetAndArtisanToolsAsTools() {
        Item cards = baseItem("playing-cards", "Набор игральных карт", "Playing Cards");
        cards.setTypes(Set.of(ItemType.GAMING_SET));

        JsonNode cardsJson = json(cards);
        assertEquals("tool", cardsJson.get("type").asText());
        assertEquals("gaming", cardsJson.get("toolCategory").asText());
        assertEquals("playing-card-set", cardsJson.get("baseToolType").asText());

        Item supplies = baseItem("artisan-s-tools", "Инструменты ремесленника", "Artisan's Tools");
        supplies.setTypes(Set.of(ItemType.ARTISAN_S_TOOLS));

        JsonNode suppliesJson = json(supplies);
        assertEquals("tool", suppliesJson.get("type").asText());
        assertEquals("artisan", suppliesJson.get("toolCategory").asText());
    }

    /** Параметры инструмента, заданные в справочнике, уезжают как есть. */
    @Test
    void exportsStoredToolFields() {
        Item item = baseItem("dice-set", "Набор костей", "Dice Set");
        item.setTypes(Set.of(ItemType.TOOL));
        Tool tool = new Tool();
        tool.setCategory("gaming");
        tool.setBaseType("dice-set");
        tool.setAbility("wisdom");
        tool.setBonus(1);
        tool.setProficiencyMode("expertise");
        item.setTool(tool);

        JsonNode json = json(item);
        assertEquals("gaming", json.get("toolCategory").asText());
        assertEquals("dice-set", json.get("baseToolType").asText());
        assertEquals("wisdom", json.get("toolAbility").asText());
        assertEquals(1, json.get("toolBonus").asInt());
        assertEquals("expertise", json.get("toolProficiencyMode").asText());
    }

    /** Категория снаряжения выводится из типов предмета, а не всегда безделушкой. */
    @Test
    void derivesEquipmentCategoryFromTypes() {
        Item ration = baseItem("rations", "Походный паёк", "Rations");
        ration.setTypes(Set.of(ItemType.FOOD_AND_DRINK));
        assertEquals("food", json(ration).get("equipmentCategory").asText());

        Item saddle = baseItem("saddle", "Седло", "Saddle");
        saddle.setTypes(Set.of(ItemType.TACK_AND_HARNESS));
        assertEquals("vehicle-equipment", json(saddle).get("equipmentCategory").asText());

        Item trinket = baseItem("lucky-charm", "Талисман", "Lucky Charm");
        assertEquals("trinket", json(trinket).get("equipmentCategory").asText());
    }

    /** Заданная в справочнике категория снаряжения перебивает вывод по типам. */
    @Test
    void prefersStoredEquipmentCategory() {
        Item item = baseItem("robe", "Мантия", "Robe");
        item.setTypes(Set.of(ItemType.ADVENTURING_GEAR));
        item.setEquipmentCategory("clothing");

        assertEquals("clothing", json(item).get("equipmentCategory").asText());
    }

    /** Заклинательная фокусировка: у справочника это тип, у системы — свойство предмета. */
    @Test
    void marksSpellcastingFocus() {
        Item focus = baseItem("component-pouch", "Мешочек с компонентами", "Component Pouch");
        focus.setTypes(Set.of(ItemType.SPELLCASTING_FOCUS));
        assertTrue(json(focus).get("isFocus").asBoolean());

        Item backpack = baseItem("backpack", "Рюкзак", "Backpack");
        assertFalse(json(backpack).has("isFocus"));
    }

    /** Активные эффекты уезжают без преобразования; пустой список в запись не пишется. */
    @Test
    void exportsActiveEffects() {
        Item item = baseItem("boots", "Сапоги", "Boots");
        ActiveEffect effect = new ActiveEffect();
        effect.setId("speed");
        effect.setName("Скорость");
        item.setActiveEffects(List.of(effect));

        JsonNode json = json(item);
        assertEquals(1, json.get("activeEffects").size());
        assertEquals("speed", json.get("activeEffects").get(0).get("id").asText());

        item.setActiveEffects(List.of());
        assertFalse(json(item).has("activeEffects"));
    }

    private JsonNode json(Item item) {
        return objectMapper.valueToTree(mapper.toVttg(item));
    }

    private Item baseItem(String url, String name, String english) {
        Item item = new Item();
        item.setUrl(url);
        item.setName(name);
        item.setEnglish(english);
        item.setDescription("");
        Source source = new Source();
        source.setAcronym("PHB");
        item.setSource(source);
        item.setSrdVersion("5.1");
        return item;
    }

    private Damage damage(Dice dice, DamageType type) {
        Damage damage = new Damage();
        damage.setRoll(roll(dice));
        damage.setType(type);
        return damage;
    }

    private DamagePart damagePart(String formula, String versatileFormula) {
        DamagePart part = new DamagePart();
        part.setFormula(formula);
        part.setVersatileFormula(versatileFormula);
        return part;
    }

    private Roll roll(Dice dice) {
        Roll roll = new Roll();
        roll.setDiceCount((short) 1);
        roll.setDice(dice);
        return roll;
    }
}

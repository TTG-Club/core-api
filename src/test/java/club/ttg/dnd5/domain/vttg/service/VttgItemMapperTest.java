package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.model.Roll;
import club.ttg.dnd5.domain.item.model.Armor;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.model.ItemType;
import club.ttg.dnd5.domain.item.model.weapon.AmmunitionType;
import club.ttg.dnd5.domain.item.model.weapon.Damage;
import club.ttg.dnd5.domain.item.model.weapon.Mastery;
import club.ttg.dnd5.domain.item.model.weapon.Property;
import club.ttg.dnd5.domain.item.model.weapon.Weapon;
import club.ttg.dnd5.domain.item.rest.dto.Range;
import club.ttg.dnd5.domain.source.model.Source;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

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
        assertEquals("trinket", json.get("equipmentCategory").asText());
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

    private Roll roll(Dice dice) {
        Roll roll = new Roll();
        roll.setDiceCount((short) 1);
        roll.setDice(dice);
        return roll;
    }
}

package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.dictionary.Rarity;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.model.Roll;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.model.weapon.Damage;
import club.ttg.dnd5.domain.item.model.weapon.Weapon;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import club.ttg.dnd5.domain.magic.model.Attunement;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.model.MagicItemCategory;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgMagicItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VttgMagicItemMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ItemRepository itemRepository = mock(ItemRepository.class);
    private final VttgMagicItemMapper mapper = new VttgMagicItemMapper(
            new VttgMarkupConverter(objectMapper),
            itemRepository,
            new VttgItemMapper(new VttgMarkupConverter(objectMapper)));

    /** Эталон SRD-бэкапа: id = "{kebab url}-{sourceKey}", type/equipmentCategory/section/флаги магичности. */
    @Test
    void mapsMagicItemToSrdBackupFormat() {
        VttgMagicItem result = mapper.toVttg(wandOfFear());

        assertEquals("wand-of-fear-dmg", result.getId());
        assertEquals("Жезл страха", result.getName());
        assertEquals("Wand of Fear", result.getNameEn());
        assertEquals("equipment", result.getType());
        assertEquals("Снаряжение", result.getTypeLabel());
        assertEquals("wand", result.getEquipmentCategory());
        assertEquals("wands", result.getSection());
        assertEquals("rare", result.getRarity());
        assertEquals("required", result.getMagicAttunement());
        assertEquals("dmg", result.getSourceKey());
        assertTrue(result.isMagical());
        assertTrue(result.isSRD());
        assertTrue(result.isReadOnly());
    }

    /** Источник в url не дублируется в id: "adamantine-armor-dmg" остаётся как есть. */
    @Test
    void doesNotDuplicateSourceSuffixAlreadyPresentInUrl() {
        MagicItem item = new MagicItem();
        item.setUrl("adamantine-armor-dmg");
        item.setCategory(MagicItemCategory.ARMOR);
        Source source = new Source();
        source.setAcronym("DMG");
        item.setSource(source);

        VttgMagicItem result = mapper.toVttg(item);

        assertEquals("adamantine-armor-dmg", result.getId());
        assertEquals("equipment", result.getType());
        assertEquals("armor", result.getSection());
        // Класс брони в модели не задан — equipmentCategory опускается.
        assertNull(result.getEquipmentCategory());
    }

    /** Магическое оружие отдаётся родным типом "weapon" в листе "weapons". */
    @Test
    void mapsMagicWeaponToWeaponType() {
        MagicItem item = new MagicItem();
        item.setUrl("flame-tongue");
        item.setCategory(MagicItemCategory.WEAPON);
        Source source = new Source();
        source.setAcronym("DMG");
        item.setSource(source);

        VttgMagicItem result = mapper.toVttg(item);

        assertEquals("flame-tongue-dmg", result.getId());
        assertEquals("weapon", result.getType());
        assertEquals("Оружие", result.getTypeLabel());
        assertEquals("weapons", result.getSection());
        assertNull(result.getEquipmentCategory());
    }

    /** Доспешные поля опускаются для не-брони — как в эталоне (жезл). */
    @Test
    void omitsArmorOnlyFieldsForNonArmorItems() {
        JsonNode json = objectMapper.valueToTree(mapper.toVttg(wandOfFear()));

        assertFalse(json.has("baseArmorAC"));
        assertFalse(json.has("maxDexBonus"));
        assertFalse(json.has("stealthDisadvantage"));
        assertFalse(json.has("strengthRequirement"));
    }

    /** Боевые поля и вес магического оружия выводятся из базового предмета по clarification. */
    @Test
    void derivesWeaponMechanicsFromBaseItemByClarification() {
        when(itemRepository.findBaseByNameForVttgExport(anyString())).thenReturn(List.of(longsword()));

        MagicItem item = new MagicItem();
        item.setUrl("flame-tongue");
        item.setName("Огненный язык");
        item.setCategory(MagicItemCategory.WEAPON);
        item.setClarification("Длинный меч");
        Source source = new Source();
        source.setAcronym("DMG");
        item.setSource(source);

        JsonNode json = objectMapper.valueToTree(mapper.toVttg(item));
        assertEquals("weapon", json.get("type").asText());
        assertEquals("longsword", json.get("baseType").asText());
        assertEquals("martial", json.get("weaponCategory").asText());
        assertEquals("1к8", json.get("damageParts").get(0).get("formula").asText());
        assertEquals("slashing", json.get("damageParts").get(0).get("type").asText());
        assertEquals(3.0, json.get("weight").asDouble());
    }

    private Item longsword() {
        Item base = new Item();
        base.setUrl("longsword");
        base.setName("Длинный меч");
        base.setEnglish("Longsword");
        base.setDescription("");
        base.setWeight("3 фунта");
        Weapon weapon = new Weapon();
        weapon.setCategory(WeaponCategory.MATERIAL_MELEE);
        Damage damage = new Damage();
        Roll roll = new Roll();
        roll.setDiceCount((short) 1);
        roll.setDice(Dice.d8);
        damage.setRoll(roll);
        damage.setType(DamageType.SLASHING);
        weapon.setDamage(damage);
        base.setWeapon(weapon);
        Source source = new Source();
        source.setAcronym("PHB");
        base.setSource(source);
        return base;
    }

    /** Бонус «+N» из названия попадает в magicBonus; без бонуса поле опускается. */
    @Test
    void parsesMagicBonusFromName() {
        MagicItem item = new MagicItem();
        item.setUrl("plate-armor-plus-1");
        item.setName("Латы, +1");
        item.setCategory(MagicItemCategory.ARMOR);
        Source source = new Source();
        source.setAcronym("DMG");
        item.setSource(source);

        assertEquals(1, mapper.toVttg(item).getMagicBonus());
        assertNull(mapper.toVttg(wandOfFear()).getMagicBonus());
    }

    private MagicItem wandOfFear() {
        MagicItem item = new MagicItem();
        item.setUrl("wand-of-fear");
        item.setName("Жезл страха");
        item.setEnglish("Wand of Fear");
        item.setDescription("Этот жезл содержит 7 зарядов...");
        item.setCategory(MagicItemCategory.WAND);
        item.setRarity(Rarity.RARE);
        Attunement attunement = new Attunement();
        attunement.setRequires(true);
        item.setAttunement(attunement);
        Source source = new Source();
        source.setAcronym("DMG");
        item.setSource(source);
        return item;
    }
}

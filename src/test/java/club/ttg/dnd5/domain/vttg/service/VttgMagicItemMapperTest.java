package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.Rarity;
import club.ttg.dnd5.domain.magic.model.Attunement;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.model.MagicItemCategory;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgMagicItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VttgMagicItemMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VttgMagicItemMapper mapper =
            new VttgMagicItemMapper(new VttgMarkupConverter(objectMapper));

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

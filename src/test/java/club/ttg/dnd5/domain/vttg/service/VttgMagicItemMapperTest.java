package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.Coin;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.dictionary.Rarity;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.model.Roll;
import club.ttg.dnd5.domain.item.model.Armor;
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

import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
        // Стоимость — по таблице редкости: редкий → 4000 зм.
        assertEquals("4000 зм", result.getCost());
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

    /** Магический предмет с varies-редкостью получает минимальную упомянутую редкость, а не "none". */
    @Test
    void derivesRarityFromVariesText() {
        MagicItem item = new MagicItem();
        item.setUrl("ioun-stone");
        item.setName("Камень Йоун");
        item.setCategory(MagicItemCategory.SUBJECT);
        item.setRarity(Rarity.VARIES);
        item.setVaries("редкость варьируется: необычный, редкий, очень редкий или легендарный");
        Source source = new Source();
        source.setAcronym("DMG");
        item.setSource(source);

        // «необычный» — минимальная упомянутая редкость; существительное «редкость» не считается за rare.
        VttgMagicItem result = mapper.toVttg(item);
        assertEquals("uncommon", result.getRarity());
        // Стоимость берётся по той же выведенной редкости: необычный → 400 зм.
        assertEquals("400 зм", result.getCost());
    }

    /** Несколько баз в clarification («полулаты или латы») раскрываются в отдельные предметы с подменой слова в названии. */
    @Test
    void splitsMultipleBaseItemsIntoSeparateEntries() {
        when(itemRepository.findBaseByNameForVttgExport("латы")).thenReturn(List.of(plate()));
        when(itemRepository.findBaseByNameForVttgExport("полулаты")).thenReturn(List.of(halfPlate()));

        MagicItem item = new MagicItem();
        item.setUrl("dwarven-plate");
        item.setName("Латы дварфов");
        item.setCategory(MagicItemCategory.ARMOR);
        item.setClarification("полулаты или латы");
        item.setRarity(Rarity.VERY_RARE);
        Source source = new Source();
        source.setAcronym("DMG");
        item.setSource(source);

        List<VttgMagicItem> variants = mapper.toVttgVariants(item, new HashMap<>());

        assertEquals(2, variants.size());
        // База, совпадающая с названием: имя и url (а значит id) не меняются.
        VttgMagicItem plate = byName(variants, "Латы дварфов");
        assertEquals("dwarven-plate-dmg", plate.getId());
        assertEquals("41500 зм", plate.getCost()); // 40000 (очень редкий) + 1500 (латы)
        // Дополнительная база: слово в названии заменено, id получает суффикс url базы.
        VttgMagicItem half = byName(variants, "Полулаты дварфов");
        assertEquals("dwarven-plate-half-plate-dmg", half.getId());
        assertEquals("40750 зм", half.getCost()); // 40000 + 750 (полулаты)
    }

    /** Один базовый предмет в clarification не расщепляется — ровно одна запись. */
    @Test
    void doesNotSplitSingleBaseItem() {
        MagicItem item = new MagicItem();
        item.setUrl("flame-tongue");
        item.setName("Огненный язык");
        item.setCategory(MagicItemCategory.WEAPON);
        item.setClarification("Длинный меч");
        Source source = new Source();
        source.setAcronym("DMG");
        item.setSource(source);

        assertEquals(1, mapper.toVttgVariants(item, new HashMap<>()).size());
    }

    private VttgMagicItem byName(List<VttgMagicItem> variants, String name) {
        return variants.stream()
                .filter(v -> name.equals(v.getName()))
                .findFirst()
                .orElseThrow();
    }

    private Item plate() {
        return armor("plate", "Латы", "Plate", "1500");
    }

    private Item halfPlate() {
        return armor("half-plate", "Полулаты", "Half Plate", "750");
    }

    private Item armor(String url, String name, String english, String cost) {
        Item base = new Item();
        base.setUrl(url);
        base.setName(name);
        base.setEnglish(english);
        base.setDescription("");
        base.setWeight("0");
        base.setCost(cost);
        base.setCoin(Coin.GC);
        base.setArmor(new Armor());
        Source source = new Source();
        source.setAcronym("PHB");
        base.setSource(source);
        return base;
    }

    /** Если clarification указывает на конкретный немагический предмет — его стоимость прибавляется к цене редкости. */
    @Test
    void addsBaseItemCostToRarityCost() {
        Item longsword = longsword();
        longsword.setCost("15");
        longsword.setCoin(Coin.GC);
        when(itemRepository.findBaseByNameForVttgExport(anyString())).thenReturn(List.of(longsword));

        MagicItem item = new MagicItem();
        item.setUrl("longsword-plus-1");
        item.setName("Длинный меч, +1");
        item.setCategory(MagicItemCategory.WEAPON);
        item.setClarification("Длинный меч");
        item.setRarity(Rarity.RARE);
        Source source = new Source();
        source.setAcronym("DMG");
        item.setSource(source);

        // 4000 (редкий) + 15 (длинный меч) = 4015 зм.
        assertEquals("4015 зм", mapper.toVttg(item).getCost());
    }

    /** Номинал монеты базового предмета приводится к золоту перед сложением. */
    @Test
    void convertsBaseItemCoinToGold() {
        Item longsword = longsword();
        longsword.setCost("5");
        longsword.setCoin(Coin.SC); // 5 см = 0.5 зм
        when(itemRepository.findBaseByNameForVttgExport(anyString())).thenReturn(List.of(longsword));

        MagicItem item = new MagicItem();
        item.setUrl("club-plus-1");
        item.setName("Дубинка, +1");
        item.setCategory(MagicItemCategory.WEAPON);
        item.setClarification("Дубинка");
        item.setRarity(Rarity.UNCOMMON);
        Source source = new Source();
        source.setAcronym("DMG");
        item.setSource(source);

        // 400 (необычный) + 0.5 (5 см) = 400.5 зм.
        assertEquals("400.5 зм", mapper.toVttg(item).getCost());
    }

    /** Стоимость выставляется по таблице «Редкость и цена» для каждого уровня редкости. */
    @Test
    void setsCostFromRarityTable() {
        assertEquals("100 зм", costForRarity(Rarity.COMMON));
        assertEquals("400 зм", costForRarity(Rarity.UNCOMMON));
        assertEquals("4000 зм", costForRarity(Rarity.RARE));
        assertEquals("40000 зм", costForRarity(Rarity.VERY_RARE));
        assertEquals("200000 зм", costForRarity(Rarity.LEGENDARY));
    }

    /** Артефакт бесценен — цена редкости не подставляется, остаётся стоимость базы (здесь — пусто). */
    @Test
    void leavesArtifactCostUnsetAsPriceless() {
        assertEquals("", costForRarity(Rarity.ARTIFACT));
    }

    private String costForRarity(Rarity rarity) {
        MagicItem item = new MagicItem();
        item.setUrl("test-item");
        item.setName("Тестовый предмет");
        item.setCategory(MagicItemCategory.SUBJECT);
        item.setRarity(rarity);
        Source source = new Source();
        source.setAcronym("DMG");
        item.setSource(source);
        return mapper.toVttg(item).getCost();
    }

    /** У магического предмета без распознаваемой редкости — дефолт, но не "none". */
    @Test
    void fallsBackToConcreteRarityWhenNotDeterminable() {
        MagicItem item = new MagicItem();
        item.setUrl("spell-scroll");
        item.setName("Свиток заклинания");
        item.setCategory(MagicItemCategory.SCROLL);
        // rarity не задана и varies нет — но isMagical=true, поэтому "none" недопустим.
        Source source = new Source();
        source.setAcronym("DMG");
        item.setSource(source);

        VttgMagicItem result = mapper.toVttg(item);
        String rarity = result.getRarity();
        assertEquals("uncommon", rarity);
        assertFalse("none".equals(rarity));
        // Дефолт «uncommon» определяет и стоимость: 400 зм.
        assertEquals("400 зм", result.getCost());
    }

    /** nameEn чистится от обрамляющих пробелов/запятых. */
    @Test
    void cleansNameEn() {
        MagicItem item = new MagicItem();
        item.setUrl("perfume-of-bewitching");
        item.setName("Духи очарования");
        item.setEnglish("  Perfume of Bewitching");
        item.setCategory(MagicItemCategory.SUBJECT);
        item.setRarity(Rarity.COMMON);
        Source source = new Source();
        source.setAcronym("DMG");
        item.setSource(source);

        assertEquals("Perfume of Bewitching", mapper.toVttg(item).getNameEn());
    }

    /** Явно связанный немагический предмет задаёт вес и стоимость (приоритет над clarification, для любой категории). */
    @Test
    void usesLinkedItemForWeightAndCost() {
        Item longsword = longsword();
        longsword.setCost("15");
        longsword.setCoin(Coin.GC);

        MagicItem item = new MagicItem();
        item.setUrl("flame-tongue");
        item.setName("Огненный язык");
        item.setCategory(MagicItemCategory.WEAPON);
        item.setRarity(Rarity.RARE);
        item.setItems(Set.of(longsword));
        Source source = new Source();
        source.setAcronym("DMG");
        item.setSource(source);

        JsonNode json = objectMapper.valueToTree(mapper.toVttg(item));
        // Вес — у связанного предмета; стоимость — цена редкости + стоимость базы.
        assertEquals(3.0, json.get("weight").asDouble());
        assertEquals("4015 зм", json.get("cost").asText()); // 4000 (редкий) + 15 (длинный меч)
    }

    /** Несколько связанных видов оружия раскрываются в отдельные записи с подменой слова в названии. */
    @Test
    void splitsByLinkedWeapons() {
        Item longsword = longsword();
        Item shortsword = shortsword();

        MagicItem item = new MagicItem();
        item.setUrl("sword-of-vengeance");
        item.setName("Длинный меч мести");
        item.setCategory(MagicItemCategory.WEAPON);
        item.setRarity(Rarity.RARE);
        item.setItems(Set.of(longsword, shortsword));
        Source source = new Source();
        source.setAcronym("DMG");
        item.setSource(source);

        List<VttgMagicItem> variants = mapper.toVttgVariants(item, new HashMap<>());

        assertEquals(2, variants.size());
        // База, совпадающая с названием — имя и url (id) не меняются.
        VttgMagicItem main = byName(variants, "Длинный меч мести");
        assertEquals("sword-of-vengeance-dmg", main.getId());
        // Дополнительная база — слово в названии заменено, id получает суффикс url базы.
        VttgMagicItem variant = byName(variants, "Короткий меч мести");
        assertEquals("sword-of-vengeance-shortsword-dmg", variant.getId());
    }

    /** Один связанный предмет не расщепляется — ровно одна запись. */
    @Test
    void doesNotSplitSingleLinkedItem() {
        MagicItem item = new MagicItem();
        item.setUrl("flame-tongue");
        item.setName("Огненный язык");
        item.setCategory(MagicItemCategory.WEAPON);
        item.setRarity(Rarity.RARE);
        item.setItems(Set.of(longsword()));
        Source source = new Source();
        source.setAcronym("DMG");
        item.setSource(source);

        assertEquals(1, mapper.toVttgVariants(item, new HashMap<>()).size());
    }

    private Item shortsword() {
        Item base = new Item();
        base.setUrl("shortsword");
        base.setName("Короткий меч");
        base.setEnglish("Shortsword");
        base.setDescription("");
        base.setWeight("2 фунта");
        Weapon weapon = new Weapon();
        weapon.setCategory(WeaponCategory.MATERIAL_MELEE);
        Damage damage = new Damage();
        Roll roll = new Roll();
        roll.setDiceCount((short) 1);
        roll.setDice(Dice.d6);
        damage.setRoll(roll);
        damage.setType(DamageType.PIERCING);
        weapon.setDamage(damage);
        base.setWeapon(weapon);
        Source source = new Source();
        source.setAcronym("PHB");
        base.setSource(source);
        return base;
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

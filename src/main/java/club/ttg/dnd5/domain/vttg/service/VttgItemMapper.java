package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.model.Roll;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.item.model.Armor;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.model.ItemCategory;
import club.ttg.dnd5.domain.item.model.ItemType;
import club.ttg.dnd5.domain.item.model.tool.Tool;
import club.ttg.dnd5.domain.item.model.weapon.AmmunitionType;
import club.ttg.dnd5.domain.item.model.weapon.Damage;
import club.ttg.dnd5.domain.item.model.weapon.DamagePart;
import club.ttg.dnd5.domain.item.model.weapon.Property;
import club.ttg.dnd5.domain.item.model.weapon.Weapon;
import club.ttg.dnd5.domain.item.rest.dto.Range;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Маппер обычного предмета TTG Club ({@link Item}) в формат компендиума VTTG ({@code GameItem}).
 *
 * <p>Тип и раздел выбираются по «начинке» предмета: наличие {@code weapon} → {@code weapon}
 * (раздел {@code weapons}); {@code armor} → {@code equipment} (раздел {@code armor}); тип-инструмент →
 * {@code tool} (раздел {@code tools}); остальное снаряжение → {@code equipment} (раздел {@code gear}).
 * Боевые/доспешные поля повторяют целевой формат SRD-бэкапа VTTG 1:1
 * (см. {@code weapons.json}/{@code armor.json}).</p>
 *
 * <p>Результат — {@code LinkedHashMap}, а не типизированный DTO: набор полей сильно зависит от рода
 * предмета (у брони, например, {@code maxDexBonus} присутствует и может быть {@code null}, тогда как
 * у оружия его быть не должно), и карта даёт точный контроль над составом ключей.</p>
 */
@Component
@RequiredArgsConstructor
public class VttgItemMapper {
    private static final Pattern LEADING_NUMBER = Pattern.compile("(\\d+(?:[.,]\\d+)?)");

    private final VttgMarkupConverter markupConverter;

    public Map<String, Object> toVttg(Item item) {
        String sourceKey = VttgSourceKeys.of(item.getSource());
        Map<String, Object> data = new LinkedHashMap<>();

        data.put("id", id(item, sourceKey));
        // Страница-источник: id несёт суффикс источника, слаг сайта — не всегда, поэтому
        // адрес ссылки из описаний выводится только из этой пары, а не из id.
        data.put("srcSection", SectionType.ITEM.getValue());
        data.put("srcUrl", item.getUrl());
        data.put("name", item.getName());
        String nameEn = cleanNameEn(item.getEnglish());
        if (nameEn != null) {
            data.put("nameEn", nameEn);
        }
        data.put("description", markupConverter.toText(item.getDescription()));

        if (isWeapon(item)) {
            putWeapon(data, item);
        } else if (isArmor(item)) {
            putArmor(data, item);
        } else if (isTool(item)) {
            putTool(data, item);
        } else {
            putGear(data, item);
        }

        data.put("quantity", 1);
        data.put("weight", weight(item.getWeight()));
        data.put("cost", cost(item));
        data.put("rarity", "none");
        data.put("equipped", false);
        data.put("isMagical", false);
        if (isFocus(item)) {
            data.put("isFocus", true);
        }
        if (!CollectionUtils.isEmpty(item.getActiveEffects())) {
            // Без преобразования: мастерская заполняет эффект сразу в вокабуляре VTTG —
            // так же, как у черты и магического предмета.
            data.put("activeEffects", item.getActiveEffects());
        }
        data.put("sourceKey", sourceKey);
        data.put("isSRD", item.getSrdVersion() != null);
        data.put("isReadOnly", true);
        return data;
    }

    // ── Оружие ──────────────────────────────────────────────────────────────────────────────────
    private void putWeapon(Map<String, Object> data, Item item) {
        Weapon weapon = item.getWeapon();

        data.put("type", "weapon");
        data.put("typeLabel", "Оружие");
        data.put("section", "weapons");
        data.put("baseType", weaponBaseType(item));
        if (weapon == null) {
            return;
        }

        String rangeType = rangeType(weapon.getCategory());
        data.put("weaponCategory", weaponCategory(weapon.getCategory()));
        data.put("rangeType", rangeType);

        List<Map<String, Object>> damageParts = damageParts(weapon);
        if (!damageParts.isEmpty()) {
            data.put("damageParts", damageParts);
        }

        boolean special = StringUtils.hasText(weapon.getAdditional());
        data.put("weaponProperties", weaponProperties(weapon.getProperties(), special));

        if (weapon.getRange() != null) {
            data.put("range", range(weapon.getRange()));
        }
        if (weapon.getAmmo() != null) {
            data.put("ammunitionType", ammunitionType(weapon.getAmmo()));
        }
        Integer reach = reach(weapon, rangeType);
        if (reach != null) {
            data.put("reach", reach);
        }
        if (special) {
            data.put("special", markupConverter.toText(weapon.getAdditional()));
        }
        if (weapon.getMastery() != null) {
            data.put("mastery", weapon.getMastery().name().toLowerCase(Locale.ROOT));
        }
        putIfPresent(data, "attackAbility", weapon.getAttackAbility());
        data.put("proficiencyMode", value(weapon.getProficiencyMode(), "auto"));
        putIfPresent(data, "attackBonus", weapon.getAttackBonus());
        putIfPresent(data, "damageAbility", weapon.getDamageAbility());
        putIfPresent(data, "damageBonus", weapon.getDamageBonus());
        putIfPresent(data, "saveType", weapon.getSaveType());
        putIfPresent(data, "saveEffect", weapon.getSaveEffect());
    }

    /**
     * Досягаемость: заданная в справочнике перебивает вывод по свойству. Дальнобойному
     * оружию досягаемость не пишется — как и раньше, если её не задали явно.
     */
    private Integer reach(Weapon weapon, String rangeType) {
        if (weapon.getReach() != null) {
            return weapon.getReach();
        }
        if (!"melee".equals(rangeType)) {
            return null;
        }
        return hasProperty(weapon.getProperties()) ? 10 : 5;
    }

    // ── Доспехи / щиты ──────────────────────────────────────────────────────────────────────────
    private void putArmor(Map<String, Object> data, Item item) {
        Armor armor = item.getArmor();

        data.put("type", "equipment");
        data.put("typeLabel", "Снаряжение");
        data.put("section", "armor");
        data.put("baseType", baseType(item));
        if (armor == null) {
            return;
        }

        data.put("baseArmorAC", armor.getArmorClass());
        // Ключ присутствует всегда (как в эталоне): null означает «без предела бонуса Ловкости».
        data.put("maxDexBonus", maxDexBonus(armor.getMod()));
        data.put("stealthDisadvantage", Boolean.TRUE.equals(armor.getStealth()));
        data.put("strengthRequirement", parseInt(armor.getStrength()));
        String category = armorCategory(armor.getCategory());
        if (category != null) {
            data.put("equipmentCategory", category);
        }
    }

    // ── Инструменты ─────────────────────────────────────────────────────────────────────────────
    private void putTool(Map<String, Object> data, Item item) {
        Tool tool = item.getTool();

        data.put("type", "tool");
        data.put("typeLabel", "Инструмент");
        data.put("section", "tools");
        data.put("toolCategory", toolCategory(item));
        data.put("baseToolType", toolBaseType(item));
        if (tool == null) {
            return;
        }
        putIfPresent(data, "toolAbility", tool.getAbility());
        putIfPresent(data, "toolBonus", tool.getBonus());
        putIfPresent(data, "toolProficiencyMode", tool.getProficiencyMode());
    }

    // ── Прочее снаряжение ───────────────────────────────────────────────────────────────────────
    private void putGear(Map<String, Object> data, Item item) {
        data.put("type", "equipment");
        data.put("typeLabel", "Снаряжение");
        data.put("section", "trinkets");
        data.put("equipmentCategory", equipmentCategory(item));
    }

    /**
     * Стабильный id: kebab-slug от {@code url} с суффиксом источника ровно один раз (как у магических
     * предметов). Если {@code url} уже оканчивается на ключ источника — повторно его не добавляем.
     */
    private String id(Item item, String sourceKey) {
        String slug = slug(item.getUrl());
        if (slug.isEmpty()) {
            return sourceKey;
        }
        return slug.equals(sourceKey) || slug.endsWith("-" + sourceKey) ? slug : slug + "-" + sourceKey;
    }

    /** Канонический английский slug базового типа (имя предмета на латинице): "Hand Crossbow" → "hand-crossbow". */
    private String baseType(Item item) {
        String base = StringUtils.hasText(item.getEnglish()) ? item.getEnglish() : item.getUrl();
        return slug(base);
    }

    /**
     * Базовый вид оружия для листа персонажа. Заданный в справочнике перебивает вывод:
     * у самодельного оружия («Клинок бури») английское название ни с одним видом не
     * сойдётся, и владение им лист не засчитает.
     *
     * <p>Дальше — ключ по адресу страницы ({@link VttgWeaponKeys}) и лишь затем слаг
     * английского названия: у канонного оружия они совпадают, а адрес ещё и переживает
     * опечатку в названии.</p>
     */
    private String weaponBaseType(Item item) {
        Weapon weapon = item.getWeapon();
        if (weapon != null && StringUtils.hasText(weapon.getBaseType())) {
            return weapon.getBaseType();
        }
        String byUrl = VttgWeaponKeys.ofUrl(item.getUrl());
        return byUrl != null ? byUrl : baseType(item);
    }

    /**
     * Базовый инструмент для листа персонажа. Слаг английского названия здесь не годится
     * сам по себе: притяжательная форма даёт «calligrapher-s-supplies», а лист знает
     * «calligraphers-supplies» и незнакомое владение молча выбрасывает — поэтому сначала
     * ключ по адресу страницы ({@link VttgToolKeys}).
     */
    private String toolBaseType(Item item) {
        Tool tool = item.getTool();
        if (tool != null && StringUtils.hasText(tool.getBaseType())) {
            return tool.getBaseType();
        }
        String byUrl = VttgToolKeys.ofUrl(item.getUrl());
        return byUrl != null ? byUrl : baseType(item);
    }

    /**
     * Кладёт значение, если оно задано: незаполненное поле справочника означает «считать
     * как раньше», и ключ с {@code null} потребитель разбирал бы как явный отказ.
     */
    private void putIfPresent(Map<String, Object> data, String key, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String text && !StringUtils.hasText(text)) {
            return;
        }
        data.put(key, value);
    }

    /** Значение справочника либо значение по умолчанию, если оно не задано. */
    private String value(String stored, String fallback) {
        return StringUtils.hasText(stored) ? stored : fallback;
    }

    private String slug(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    /**
     * Нормализует английское имя для выдачи: убирает обрамляющие пробелы/запятые и схлопывает
     * повторяющиеся пробелы внутри (в данных встречается мусор вида "  Perfume of Bewitching").
     * Возвращает {@code null}, если после чистки имя пустое (поле опускается).
     */
    static String cleanNameEn(String english) {
        if (!StringUtils.hasText(english)) {
            return null;
        }
        String cleaned = english.replaceAll("\\s+", " ")
                .replaceAll("^[\\s,]+|[\\s,]+$", "")
                .trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private String weaponCategory(WeaponCategory category) {
        if (category == null) {
            return "simple";
        }
        return switch (category) {
            case SIMPLE_MELEE, SIMPLE_RANGED -> "simple";
            case MATERIAL_MELEE, MATERIAL_RANGED, FIREARM, FUTURISTIC -> "martial";
        };
    }

    private String rangeType(WeaponCategory category) {
        if (category == null) {
            return "melee";
        }
        return switch (category) {
            case SIMPLE_RANGED, MATERIAL_RANGED, FIREARM, FUTURISTIC -> "ranged";
            case SIMPLE_MELEE, MATERIAL_MELEE -> "melee";
        };
    }

    /**
     * Части урона в формате VTTG (как у заклинаний). Заданные в справочнике отдаются как
     * есть: мастерская пишет их сразу формулами вокабуляра VTTG, переводить нечего.
     * Пусто — собираем одну часть из прежней связки «кости + тип», как раньше: девять
     * сотен записей справочника заполнены именно ею.
     */
    private List<Map<String, Object>> damageParts(Weapon weapon) {
        if (!CollectionUtils.isEmpty(weapon.getDamageParts())) {
            return weapon.getDamageParts().stream()
                    .filter(part -> part != null && StringUtils.hasText(part.getFormula()))
                    .map(this::damagePart)
                    .toList();
        }
        return legacyDamageParts(weapon);
    }

    /**
     * Часть урона справочника в формате VTTG; незаполненные поля не пишутся.
     *
     * <p>Доступна соседям по пакету: магический предмет дописывает к частям базового
     * оружия свои ({@code VttgMagicItemMapper}) и переводит их той же функцией.</p>
     */
    Map<String, Object> damagePart(DamagePart part) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("formula", part.getFormula());
        putIfPresent(result, "target", part.getTarget());
        if (Boolean.TRUE.equals(part.getRequiresDamage())) {
            result.put("requiresDamage", true);
        }
        putIfPresent(result, "versatileFormula", part.getVersatileFormula());
        return result;
    }

    /**
     * Часть урона из прежней связки «кости + тип»: кость через «к», тип урона — slug SRD,
     * {@code versatileFormula} — для универсального (versatile) оружия. Бонус для оружия не используется.
     */
    private List<Map<String, Object>> legacyDamageParts(Weapon weapon) {
        Damage damage = weapon.getDamage();
        String formula = damage == null ? null : damageFormula(damage.getRoll());
        if (formula == null) {
            return List.of();
        }
        Map<String, Object> part = new LinkedHashMap<>();
        part.put("formula", formula);
        if (damage.getType() != null) {
            part.put("type", damageType(damage.getType()));
        }
        String versatile = damageFormula(weapon.getVersatile());
        if (versatile != null) {
            part.put("versatileFormula", versatile);
        }
        return List.of(part);
    }

    /** Кубик урона в формате VTTG («2к6»); бонус для оружия не используется. */
    private String damageFormula(Roll roll) {
        if (roll == null || roll.getDice() == null) {
            return null;
        }
        int count = roll.getDiceCount() == null ? 1 : roll.getDiceCount();
        return count + "к" + roll.getDice().getMaxValue();
    }

    private String damageType(DamageType type) {
        return type.name().toLowerCase(Locale.ROOT);
    }

    private List<String> weaponProperties(Set<Property> properties, boolean special) {
        Set<String> slugs = new TreeSet<>();
        if (properties != null) {
            properties.stream()
                    .filter(Objects::nonNull)
                    .map(this::propertySlug)
                    .filter(Objects::nonNull)
                    .forEach(slugs::add);
        }
        if (special) {
            slugs.add("special");
        }
        return new ArrayList<>(slugs);
    }

    private String propertySlug(Property property) {
        return switch (property) {
            case AMMUNITION -> "ammunition";
            case FINESSE -> "finesse";
            case HEAVY -> "heavy";
            case LIGHT -> "light";
            case LOADING -> "loading";
            case REACH -> "reach";
            case THROWN -> "thrown";
            case TWO_HANDED -> "two-handed";
            case VERSATILE -> "versatile";
            // Нет точного соответствия в формате VTTG — опускаем.
            case RANGE, BURST_FIRE, MAGAZINE -> null;
        };
    }

    private boolean hasProperty(Set<Property> properties) {
        return properties != null && properties.contains(Property.REACH);
    }

    private Map<String, Object> range(Range range) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("normal", (int) range.getNormal());
        if (range.getMax() != null) {
            result.put("long", range.getMax().intValue());
        }
        return result;
    }

    /**
     * Тип боеприпаса в словаре системы VTTG (`AmmunitionType`).
     *
     * <p>Ключи взяты у потребителя дословно: у него это {@code arrows},
     * {@code bolts}, {@code bullets}, {@code sling-bullets} и
     * {@code blowgun-needles}. Снаряд пращи и игла для трубки — свои позиции, а
     * не синонимы пуль: иначе праща и духовая трубка приезжают с чужим
     * боеприпасом и расход патронов на листе не работает.</p>
     */
    private String ammunitionType(AmmunitionType ammo) {
        return switch (ammo) {
            case ARROW -> "arrows";
            case BOLT -> "bolts";
            case BULLET -> "bullets";
            case SLING_BULLET -> "sling-bullets";
            case NEEDLE -> "blowgun-needles";
        };
    }

    private Integer maxDexBonus(Armor.DexterityMod mod) {
        if (mod == null) {
            return null;
        }
        return switch (mod) {
            case PLUS -> null;        // без предела
            case PLUS_MAX_2 -> 2;
            case NONE -> 0;
        };
    }

    private String armorCategory(ArmorCategory category) {
        if (category == null) {
            return null;
        }
        return switch (category) {
            case LIGHT -> "light";
            case MEDIUM -> "medium";
            case HEAVY -> "heavy";
            case SHIELD -> "shield";
        };
    }

    private boolean isWeapon(Item item) {
        return item.getCategory() == ItemCategory.WEAPON || hasTypeCategory(item, ItemCategory.WEAPON);
    }

    private boolean isArmor(Item item) {
        return item.getCategory() == ItemCategory.ARMOR || hasTypeCategory(item, ItemCategory.ARMOR);
    }

    /**
     * Инструмент ли предмет. Категории {@code TOOL} мало: игровой набор и инструменты
     * ремесленника заведены типами категории {@code ITEM}, и «Набор игральных карт»
     * уезжал на стол безделушкой — владение таким набором лист не засчитывал.
     */
    private boolean isTool(Item item) {
        return item.getCategory() == ItemCategory.TOOL
                || hasTypeCategory(item, ItemCategory.TOOL)
                || hasType(item, ItemType.ARTISAN_S_TOOLS)
                || hasType(item, ItemType.GAMING_SET);
    }

    private boolean hasTypeCategory(Item item, ItemCategory category) {
        return item.getTypes() != null && item.getTypes().stream()
                .filter(Objects::nonNull)
                .anyMatch(type -> type.getCategory() == category);
    }

    /**
     * Категория инструмента. Заданная в справочнике перебивает вывод по типам предмета;
     * иначе разбираем типы: у листа четыре категории, и «прочее» для набора ремесленника
     * значит потерянную группу в окне владений.
     */
    private String toolCategory(Item item) {
        Tool tool = item.getTool();
        if (tool != null && StringUtils.hasText(tool.getCategory())) {
            return tool.getCategory();
        }
        if (hasType(item, ItemType.INSTRUMENT)) {
            return "musical";
        }
        if (hasType(item, ItemType.ARTISAN_S_TOOLS)) {
            return "artisan";
        }
        if (hasType(item, ItemType.GAMING_SET)) {
            return "gaming";
        }
        return "other";
    }

    /**
     * Категория снаряжения для листа и виртуального стола. Заданная в справочнике
     * перебивает вывод по типам предмета; иначе разбираем типы, а безделушка остаётся
     * последним вариантом, а не единственным, как было раньше.
     */
    private String equipmentCategory(Item item) {
        if (StringUtils.hasText(item.getEquipmentCategory())) {
            return item.getEquipmentCategory();
        }
        if (hasType(item, ItemType.FOOD_AND_DRINK)) {
            return "food";
        }
        if (hasType(item, ItemType.VEHICLE) || hasType(item, ItemType.VEHICLE_AIR)
                || hasType(item, ItemType.VEHICLE_LAND) || hasType(item, ItemType.VEHICLE_WATER)
                || hasType(item, ItemType.TACK_AND_HARNESS)) {
            return "vehicle-equipment";
        }
        if (hasType(item, ItemType.ADVENTURING_GEAR) || hasType(item, ItemType.AMMUNITION)
                || hasType(item, ItemType.POISON) || hasType(item, ItemType.SIEGE_EQUIPMENT)
                || hasType(item, ItemType.SPELLCASTING_FOCUS)) {
            return "adventurer-equipment";
        }
        return "trinket";
    }

    /**
     * Заклинательная фокусировка: у системы это свойство предмета, а у справочника —
     * его тип. Раньше признак не выдавался вовсе, и посох друида на столе фокусировкой
     * не считался.
     */
    private boolean isFocus(Item item) {
        return hasType(item, ItemType.SPELLCASTING_FOCUS);
    }

    private boolean hasType(Item item, ItemType type) {
        return item.getTypes() != null && item.getTypes().contains(type);
    }

    private double weight(String weight) {
        if (!StringUtils.hasText(weight)) {
            return 0;
        }
        Matcher matcher = LEADING_NUMBER.matcher(weight);
        if (!matcher.find()) {
            return 0;
        }
        return Double.parseDouble(matcher.group(1).replace(',', '.'));
    }

    private int parseInt(String value) {
        if (!StringUtils.hasText(value)) {
            return 0;
        }
        Matcher matcher = LEADING_NUMBER.matcher(value);
        return matcher.find() ? Integer.parseInt(matcher.group(1).split("[.,]")[0]) : 0;
    }

    /** Стоимость в формате эталона ("15 зм"); пусто, если не задана. */
    private String cost(Item item) {
        if (!StringUtils.hasText(item.getCost()) || item.getCoin() == null) {
            return "";
        }
        return item.getCost() + " " + item.getCoin().getShortName();
    }

}

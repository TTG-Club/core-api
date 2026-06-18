package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.model.Roll;
import club.ttg.dnd5.domain.item.model.Armor;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.model.ItemCategory;
import club.ttg.dnd5.domain.item.model.ItemType;
import club.ttg.dnd5.domain.item.model.weapon.AmmunitionType;
import club.ttg.dnd5.domain.item.model.weapon.Damage;
import club.ttg.dnd5.domain.item.model.weapon.Property;
import club.ttg.dnd5.domain.item.model.weapon.Weapon;
import club.ttg.dnd5.domain.item.rest.dto.Range;
import club.ttg.dnd5.domain.source.model.Source;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
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
    /** Запасной ключ источника, если у предмета его нет. */
    private static final String SOURCE = "srd";
    private static final Pattern LEADING_NUMBER = Pattern.compile("(\\d+(?:[.,]\\d+)?)");

    private final VttgMarkupConverter markupConverter;

    public Map<String, Object> toVttg(Item item) {
        String sourceKey = sourceKey(item.getSource());
        Map<String, Object> data = new LinkedHashMap<>();

        data.put("id", id(item, sourceKey));
        data.put("name", item.getName());
        String nameEn = cleanNameEn(item.getEnglish());
        if (nameEn != null) {
            data.put("nameEn", nameEn);
        }
        data.put("description", markupConverter.toText(item.getDescription()));

        if (item.getWeapon() != null) {
            putWeapon(data, item);
        } else if (item.getArmor() != null) {
            putArmor(data, item);
        } else if (isTool(item)) {
            putTool(data, item);
        } else {
            putGear(data);
        }

        data.put("quantity", 1);
        data.put("weight", weight(item.getWeight()));
        data.put("cost", cost(item));
        data.put("rarity", "none");
        data.put("equipped", false);
        data.put("isMagical", false);
        data.put("sourceKey", sourceKey);
        data.put("isSRD", true);
        data.put("isReadOnly", true);
        return data;
    }

    // ── Оружие ──────────────────────────────────────────────────────────────────────────────────
    private void putWeapon(Map<String, Object> data, Item item) {
        Weapon weapon = item.getWeapon();
        String rangeType = rangeType(weapon.getCategory());

        data.put("type", "weapon");
        data.put("typeLabel", "Оружие");
        data.put("section", "weapons");
        data.put("baseType", baseType(item));
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
        if ("melee".equals(rangeType)) {
            data.put("reach", hasProperty(weapon.getProperties()) ? 10 : 5);
        }
        if (special) {
            data.put("special", markupConverter.toText(weapon.getAdditional()));
        }
        if (weapon.getMastery() != null) {
            data.put("mastery", weapon.getMastery().name().toLowerCase(Locale.ROOT));
        }
        data.put("proficiencyMode", "auto");
    }

    // ── Доспехи / щиты ──────────────────────────────────────────────────────────────────────────
    private void putArmor(Map<String, Object> data, Item item) {
        Armor armor = item.getArmor();

        data.put("type", "equipment");
        data.put("typeLabel", "Снаряжение");
        data.put("section", "armor");
        data.put("baseType", baseType(item));
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
        data.put("type", "tool");
        data.put("typeLabel", "Инструмент");
        data.put("section", "tools");
        data.put("toolCategory", toolCategory(item));
        data.put("baseToolType", baseType(item));
    }

    // ── Прочее снаряжение ───────────────────────────────────────────────────────────────────────
    private void putGear(Map<String, Object> data) {
        data.put("type", "equipment");
        data.put("typeLabel", "Снаряжение");
        data.put("section", "trinkets");
        data.put("equipmentCategory", "trinket");
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
     * Части урона в формате VTTG (как у заклинаний): кость через «к», тип урона — slug SRD,
     * {@code versatileFormula} — для универсального (versatile) оружия. Бонус для оружия не используется.
     */
    private List<Map<String, Object>> damageParts(Weapon weapon) {
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
        return type == DamageType.FAIR ? "fire" : type.name().toLowerCase(Locale.ROOT);
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
            case RANGE, BURST_FIRE -> null;
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

    private String ammunitionType(AmmunitionType ammo) {
        return switch (ammo) {
            case ARROW -> "arrows";
            case BOLT -> "bolts";
            case BULLET, SLING_BULLET -> "bullets";
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

    private boolean isTool(Item item) {
        return item.getTypes() != null && item.getTypes().stream()
                .filter(Objects::nonNull)
                .anyMatch(type -> type.getCategory() == ItemCategory.TOOL);
    }

    private String toolCategory(Item item) {
        if (item.getTypes() != null && item.getTypes().contains(ItemType.INSTRUMENT)) {
            return "musical";
        }
        return "other";
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

    private String sourceKey(Source source) {
        if (source == null) {
            return SOURCE;
        }
        if ("PHB24".equalsIgnoreCase(source.getAcronym())) {
            return "phb";
        }
        return StringUtils.hasText(source.getAcronym())
                ? source.getAcronym().toLowerCase(Locale.ROOT)
                : SOURCE;
    }
}

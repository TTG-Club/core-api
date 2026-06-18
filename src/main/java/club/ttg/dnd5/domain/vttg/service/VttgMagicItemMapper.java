package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.Rarity;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import club.ttg.dnd5.domain.magic.model.Attunement;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.model.MagicItemCategory;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgMagicItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Маппер магического предмета TTG Club в формат компендиума VTTG ({@code GameItem}).
 *
 * <p>Тип отдаётся родной: оружие → {@code weapon}, всё остальное → {@code equipment} с флагом
 * {@code isMagical=true} (отдельного типа «magic-item» нет). {@code section} раскладывает запись
 * по листу дерева разделов (weapons/armor/rings/wands/wondrous).</p>
 *
 * <p>Сопоставление справочников выполнено под перечисления VTTG ({@code EquipmentCategory},
 * {@code ItemRarity}). Категории без точного соответствия отображаются на близкий аналог
 * (жезл/посох → wand, зелье/свиток → wondrous). Структурных данных оружия/доспеха в самой модели
 * {@code MagicItem} нет, поэтому боевые/доспешные поля (а также вес и стоимость) выводятся из
 * базового предмета {@link Item}, найденного по уточнению ({@code clarification}); для «общих»
 * зачарований и неразрешённых уточнений эти поля опускаются (как в эталоне SRD-бэкапа).</p>
 */
@Component
@RequiredArgsConstructor
public class VttgMagicItemMapper {
    /** Запасной ключ источника, если у предмета его нет. */
    private static final String SOURCE = "srd";
    /** Бонус «+1/+2/+3» из названия/уточнения предмета. */
    private static final Pattern MAGIC_BONUS = Pattern.compile("\\+([123])");

    /** Боевые поля базового оружия, переносимые в магическое оружие из {@code VttgItemMapper}. */
    private static final List<String> WEAPON_KEYS = List.of(
            "baseType", "weaponCategory", "rangeType", "damageParts", "weaponProperties",
            "reach", "range", "ammunitionType", "mastery", "proficiencyMode", "special");
    /** Доспешные поля базовой брони, переносимые в магическую броню. */
    private static final List<String> ARMOR_KEYS = List.of(
            "baseType", "equipmentCategory", "baseArmorAC", "maxDexBonus",
            "stealthDisadvantage", "strengthRequirement");

    private final VttgMarkupConverter markupConverter;
    private final ItemRepository itemRepository;
    private final VttgItemMapper itemMapper;

    public VttgMagicItem toVttg(MagicItem item) {
        Attunement attunement = item.getAttunement();
        boolean requiresAttunement = attunement != null && attunement.isRequires();
        String sourceKey = sourceKey(item.getSource());
        MagicItemCategory category = item.getCategory();
        boolean weapon = category == MagicItemCategory.WEAPON;
        BaseMechanics base = resolveBase(item, category);

        return VttgMagicItem.builder()
                .id(id(item, sourceKey))
                .name(item.getName())
                .nameEn(item.getEnglish())
                // VTTG сам отрисовывает {@roll ...} в описании предмета — сохраняем эти теги.
                .description(markupConverter.toTextKeepingRolls(item.getDescription()))
                // Оружие отдаём родным типом "weapon", всё остальное — "equipment" (п.3 контракта).
                .type(weapon ? "weapon" : "equipment")
                .typeLabel(weapon ? "Оружие" : "Снаряжение")
                .section(section(category))
                .quantity(1)
                // Вес/стоимость берём у базового предмета (по clarification); иначе 0/"".
                .weight(base.weight())
                .cost(base.cost())
                .rarity(rarity(item.getRarity()))
                .equipped(false)
                // У оружия своя категория (weaponCategory); для брони — из mechanics; иначе реальная.
                .equipmentCategory(weapon ? null : equipmentCategory(category))
                // Боевые/доспешные поля выводятся из базового предмета (см. resolveBase);
                // для «общих» зачарований и нерешённых уточнений их нет — это допустимо.
                .mechanics(base.fields())
                .isMagical(true)
                .magicAttunement(requiresAttunement ? "required" : "none")
                .magicBonus(magicBonus(item))
                .sourceKey(sourceKey)
                .isSRD(true)
                .isReadOnly(true)
                .build();
    }

    /**
     * Стабильный id в формате VTTG: kebab-slug от {@code url} с суффиксом источника ровно один раз,
     * например "wand-of-fear-dmg". Если {@code url} уже оканчивается на ключ источника
     * (как в данных TTG: "adamantine-armor-dmg"), повторно его не добавляем — иначе VTTG создаст дубль.
     *
     * <p>Slug — латиница/цифры/дефис (имя файла у VTTG = id).</p>
     */
    private String id(MagicItem item, String sourceKey) {
        String slug = slug(item.getUrl());
        if (slug.isEmpty()) {
            return sourceKey;
        }
        return slug.equals(sourceKey) || slug.endsWith("-" + sourceKey) ? slug : slug + "-" + sourceKey;
    }

    /**
     * Разрешает базовый предмет по уточнению ({@code clarification}) или названию и переносит из него
     * боевые/доспешные поля и вес/стоимость. Срабатывает только для оружия и брони; для «общих»
     * зачарований и уточнений без точного совпадения базы возвращает пустой результат.
     */
    private BaseMechanics resolveBase(MagicItem item, MagicItemCategory category) {
        if (category != MagicItemCategory.WEAPON && category != MagicItemCategory.ARMOR) {
            return BaseMechanics.EMPTY;
        }
        Item base = findBase(item, category);
        if (base == null) {
            return BaseMechanics.EMPTY;
        }
        Map<String, Object> baseMap = itemMapper.toVttg(base);
        Map<String, Object> fields = new LinkedHashMap<>();
        for (String key : category == MagicItemCategory.WEAPON ? WEAPON_KEYS : ARMOR_KEYS) {
            if (baseMap.containsKey(key)) {
                fields.put(key, baseMap.get(key));
            }
        }
        double weight = baseMap.get("weight") instanceof Number number ? number.doubleValue() : 0;
        String cost = baseMap.get("cost") instanceof String value ? value : "";
        return new BaseMechanics(weight, cost, fields.isEmpty() ? null : fields);
    }

    /** Первый видимый базовый предмет нужного рода (оружие/броня) с точным совпадением имени. */
    private Item findBase(MagicItem item, MagicItemCategory category) {
        boolean needWeapon = category == MagicItemCategory.WEAPON;
        for (String candidate : new String[]{item.getClarification(), item.getName(), item.getEnglish()}) {
            if (!StringUtils.hasText(candidate)) {
                continue;
            }
            for (Item base : itemRepository.findBaseByNameForVttgExport(candidate.trim())) {
                if (needWeapon ? base.getWeapon() != null : base.getArmor() != null) {
                    return base;
                }
            }
        }
        return null;
    }

    /** Бонус оружия/брони «+1/+2/+3» из названия, английского имени или уточнения; {@code null} — нет бонуса. */
    private Integer magicBonus(MagicItem item) {
        return firstBonus(item.getName(), item.getEnglish(), item.getClarification());
    }

    private Integer firstBonus(String... values) {
        for (String value : values) {
            if (!StringUtils.hasText(value)) {
                continue;
            }
            Matcher matcher = MAGIC_BONUS.matcher(value);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        return null;
    }

    private String slug(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    /** Лист дерева разделов VTTG, в который попадёт предмет (см. {@code VttgCompendiumSections}). */
    private String section(MagicItemCategory category) {
        if (category == null) {
            return "wondrous";
        }
        return switch (category) {
            case WEAPON -> "weapons";
            case ARMOR -> "armor";
            case RING -> "rings";
            case WAND, ROD, STAFF -> "wands";
            case POTION, SCROLL, SUBJECT -> "wondrous";
        };
    }

    /** MagicItemCategory → EquipmentCategory VTTG (ring|wand|wondrous|light|medium|heavy|shield|clothing...). */
    private String equipmentCategory(MagicItemCategory category) {
        if (category == null) {
            return "wondrous";
        }
        return switch (category) {
            case RING -> "ring";
            case WAND, ROD, STAFF -> "wand";   // нет отдельных rod/staff — implement-аналог
            case ARMOR -> null;                // класс брони (light|medium|heavy|shield) в модели не задан
            case POTION, SCROLL, SUBJECT -> "wondrous";
            case WEAPON -> null;               // обрабатывается как type=weapon
        };
    }

    /** Rarity → ItemRarity VTTG (none|common|uncommon|rare|very-rare|legendary|artifact). */
    private String rarity(Rarity rarity) {
        if (rarity == null) {
            return "none";
        }
        return switch (rarity) {
            case COMMON -> "common";
            case UNCOMMON -> "uncommon";
            case RARE -> "rare";
            case VERY_RARE -> "very-rare";
            case LEGENDARY -> "legendary";
            case ARTIFACT -> "artifact";
            case VARIES, UNKNOWN -> "none";   // в ItemRarity нет «varies»/«unknown»
        };
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

    /** Выведенные из базового предмета поля: вес, стоимость и боевые/доспешные поля ({@code null} — нет). */
    private record BaseMechanics(double weight, String cost, Map<String, Object> fields) {
        private static final BaseMechanics EMPTY = new BaseMechanics(0, "", null);
    }
}

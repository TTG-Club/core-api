package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.Coin;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
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
    /** Дефолтная редкость магического предмета, когда реальную не удалось определить (none недопустим). */
    private static final Rarity DEFAULT_MAGIC_RARITY = Rarity.UNCOMMON;
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
        return toVttg(item, new HashMap<>());
    }

    /**
     * Вариант для пакетной выгрузки: {@code baseCache} мемоизирует разрешение базовых предметов
     * по уточнению/названию в пределах одного ответа. Множество зачарований «+1/+2/+3» делят одну
     * базу (например «длинный меч»), поэтому кэш убирает повторные сканы таблицы {@code item}.
     *
     * <p>Маппит предмет «как есть», без расщепления по нескольким базам в {@code clarification}
     * (для этого см. {@link #toVttgVariants}).</p>
     */
    public VttgMagicItem toVttg(MagicItem item, Map<String, List<Item>> baseCache) {
        return map(item, item.getName(), item.getClarification(), item.getUrl(), item.getEnglish(), baseCache);
    }

    /**
     * Раскрывает предмет в один или несколько экспортируемых: если в {@code clarification} перечислено
     * несколько базовых предметов (напр. «полулаты или латы»), а в названии присутствует один из них,
     * на каждый базовый предмет создаётся отдельная запись с подменой этого слова в названии
     * (напр. «Латы дварфов» → «Латы дварфов» и «Полулаты дварфов»). Иначе — ровно одна запись.
     */
    public List<VttgMagicItem> toVttgVariants(MagicItem item, Map<String, List<Item>> baseCache) {
        MagicItemCategory category = item.getCategory();
        List<String> bases = splitBaseItems(item.getClarification());
        // Расщепляем только оружие/броню — лишь для них ищем конкретный предмет в таблице item.
        boolean splittable = category == MagicItemCategory.WEAPON || category == MagicItemCategory.ARMOR;
        String anchor = splittable && bases.size() > 1 ? anchor(item.getName(), bases) : null;
        if (anchor == null) {
            return List.of(toVttg(item, baseCache));
        }
        List<VttgMagicItem> result = new ArrayList<>(bases.size());
        for (String base : bases) {
            if (base.equalsIgnoreCase(anchor)) {
                // Базовый предмет, совпадающий с названием — исходные имя/url/english (контракт не меняется).
                result.add(map(item, item.getName(), base, item.getUrl(), item.getEnglish(), baseCache));
            } else {
                String name = replaceAnchor(item.getName(), anchor, base);
                result.add(map(item, name, base, variantUrl(item, base, baseCache), null, baseCache));
            }
        }
        return result;
    }

    /**
     * Payload для дельты {@code /changes}: один объект для обычного предмета либо список, если предмет
     * расщепляется на несколько (см. {@link #toVttgVariants}). Список сохраняется в payload массивом и
     * затем разворачивается в отдельные изменения ({@link VttgPayloadStore}); обычные предметы остаются
     * объектом — их контракт не меняется.
     */
    public Object toVttgPayload(MagicItem item, Map<String, List<Item>> baseCache) {
        List<VttgMagicItem> variants = toVttgVariants(item, baseCache);
        return variants.size() == 1 ? variants.get(0) : variants;
    }

    /** Сборка одной записи VTTG для заданных имени/уточнения/url/английского названия. */
    private VttgMagicItem map(MagicItem item, String name, String clarification, String url,
                              String english, Map<String, List<Item>> baseCache) {
        Attunement attunement = item.getAttunement();
        boolean requiresAttunement = attunement != null && attunement.isRequires();
        String sourceKey = sourceKey(item.getSource());
        MagicItemCategory category = item.getCategory();
        boolean weapon = category == MagicItemCategory.WEAPON;
        Item base = findBase(category, baseCache, clarification, name, english);
        BaseMechanics mechanics = mechanics(base, category);
        Rarity rarity = effectiveRarity(item);

        return VttgMagicItem.builder()
                .id(id(url, sourceKey))
                .name(name)
                .nameEn(VttgItemMapper.cleanNameEn(english))
                // VTTG сам отрисовывает {@roll ...} в описании предмета — сохраняем эти теги.
                .description(markupConverter.toTextKeepingRolls(item.getDescription()))
                // Оружие отдаём родным типом "weapon", всё остальное — "equipment" (п.3 контракта).
                .type(weapon ? "weapon" : "equipment")
                .typeLabel(weapon ? "Оружие" : "Снаряжение")
                .section(section(category))
                .quantity(1)
                // Вес берём у базового предмета (по clarification); иначе 0.
                .weight(mechanics.weight())
                // Стоимость — по редкости (таблица «Редкость и цена»); артефакт бесценен — стоимость базы (обычно "").
                .cost(cost(rarity, mechanics))
                .rarity(rarityCode(rarity))
                .equipped(false)
                // У оружия своя категория (weaponCategory); для брони — из mechanics; иначе реальная.
                .equipmentCategory(weapon ? null : equipmentCategory(category))
                // Боевые/доспешные поля выводятся из базового предмета (см. mechanics);
                // для «общих» зачарований и нерешённых уточнений их нет — это допустимо.
                .mechanics(mechanics.fields())
                .isMagical(true)
                .magicAttunement(requiresAttunement ? "required" : "none")
                .magicBonus(firstBonus(name, english, clarification))
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
    private String id(String url, String sourceKey) {
        String slug = slug(url);
        if (slug.isEmpty()) {
            return sourceKey;
        }
        return slug.equals(sourceKey) || slug.endsWith("-" + sourceKey) ? slug : slug + "-" + sourceKey;
    }

    /**
     * Url дополнительного варианта: к url предмета добавляется slug url базового предмета
     * (напр. «dwarven-plate» + «half-plate» → «dwarven-plate-half-plate»), чтобы id был уникальным
     * и стабильным. Если базовый предмет не разрешился (нелатинский/пустой slug) — добавляем сам base.
     */
    private String variantUrl(MagicItem item, String base, Map<String, List<Item>> baseCache) {
        Item resolved = findBase(item.getCategory(), baseCache, base);
        String suffix = resolved != null ? slug(resolved.getUrl()) : "";
        if (suffix.isEmpty()) {
            suffix = slug(base);
        }
        return suffix.isEmpty() ? item.getUrl() : item.getUrl() + "-" + suffix;
    }

    /** Боевые/доспешные поля и вес/стоимость базового предмета; {@code EMPTY} — база не найдена. */
    private BaseMechanics mechanics(Item base, MagicItemCategory category) {
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
        return new BaseMechanics(weight, goldCost(base), fields.isEmpty() ? null : fields);
    }

    /**
     * Первый видимый базовый предмет нужного рода (оружие/броня) с точным совпадением имени по любому
     * из {@code candidates} (уточнение/название/английское имя). Только для оружия и брони.
     */
    private Item findBase(MagicItemCategory category, Map<String, List<Item>> baseCache, String... candidates) {
        if (category != MagicItemCategory.WEAPON && category != MagicItemCategory.ARMOR) {
            return null;
        }
        boolean needWeapon = category == MagicItemCategory.WEAPON;
        for (String candidate : candidates) {
            if (!StringUtils.hasText(candidate)) {
                continue;
            }
            String key = candidate.trim().toLowerCase(Locale.ROOT);
            List<Item> found =
                    baseCache.computeIfAbsent(key, ignored -> itemRepository.findBaseByNameForVttgExport(candidate.trim()));
            for (Item base : found) {
                if (needWeapon ? base.getWeapon() != null : base.getArmor() != null) {
                    return base;
                }
            }
        }
        return null;
    }

    /** Разбивает {@code clarification} на отдельные базовые предметы по «или»/запятой/точке с запятой. */
    private List<String> splitBaseItems(String clarification) {
        if (!StringUtils.hasText(clarification)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String part : clarification.split("(?i)\\s+или\\s+|\\s*[,;]\\s*")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /** Самый длинный базовый предмет из {@code bases}, входящий в название (без учёта регистра); иначе {@code null}. */
    private String anchor(String name, List<String> bases) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        String lower = name.toLowerCase(Locale.ROOT);
        String best = null;
        for (String base : bases) {
            if (lower.contains(base.toLowerCase(Locale.ROOT))
                    && (best == null || base.length() > best.length())) {
                best = base;
            }
        }
        return best;
    }

    /** Подменяет вхождение {@code anchor} в названии на {@code base}, сохраняя регистр первой буквы. */
    private String replaceAnchor(String name, String anchor, String base) {
        int idx = name.toLowerCase(Locale.ROOT).indexOf(anchor.toLowerCase(Locale.ROOT));
        if (idx < 0) {
            return name;
        }
        String matched = name.substring(idx, idx + anchor.length());
        String replacement = !matched.isEmpty() && Character.isUpperCase(matched.charAt(0))
                ? Character.toUpperCase(base.charAt(0)) + base.substring(1)
                : base;
        return name.substring(0, idx) + replacement + name.substring(idx + anchor.length());
    }

    /** Бонус оружия/брони «+1/+2/+3» из названия, английского имени или уточнения; {@code null} — нет бонуса. */
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

    /**
     * Стоимость предмета: цена по таблице «Редкость и цена» ({@link Rarity#getBaseCost()}) плюс
     * стоимость базового немагического предмета, разрешённого по {@code clarification}
     * (напр. «длинный меч», «латы»), приведённая к золоту. Артефакт бесценен
     * ({@code baseCost == null}) — для него цена редкости не подставляется, остаётся лишь стоимость
     * базы (обычно её нет).
     */
    private String cost(Rarity rarity, BaseMechanics base) {
        Integer rarityCost = rarity.getBaseCost();
        Double baseCost = base.costGold();
        if (rarityCost == null) {
            return baseCost != null ? formatGold(baseCost) : "";
        }
        return formatGold(rarityCost + (baseCost != null ? baseCost : 0));
    }

    /** Стоимость базы в золоте (с учётом номинала монеты); {@code null} — не задана или не парсится. */
    private Double goldCost(Item base) {
        if (!StringUtils.hasText(base.getCost()) || base.getCoin() == null) {
            return null;
        }
        String normalized = base.getCost().replaceAll("[\\s\\u00A0]", "").replace(',', '.');
        try {
            return Double.parseDouble(normalized) * base.getCoin().getExchangeForGold();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Золото в формате эталона: «4015 зм»; дробные суммы округляются до сотых (медь) без хвостовых нулей. */
    private String formatGold(double gold) {
        String number = BigDecimal.valueOf(gold)
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
        return number + " " + Coin.GC.getShortName();
    }

    /**
     * Реальная редкость магического предмета. У {@code isMagical=true} редкость обязана быть
     * конкретной (none недопустим), поэтому для VARIES/UNKNOWN/отсутствующей она выводится из текста
     * {@code varies} (берётся минимальная упомянутая редкость), а если распознать не удалось —
     * подставляется дефолт {@link #DEFAULT_MAGIC_RARITY}.
     */
    private Rarity effectiveRarity(MagicItem item) {
        Rarity rarity = item.getRarity();
        if (rarity != null && rarity != Rarity.VARIES && rarity != Rarity.UNKNOWN) {
            return rarity;
        }
        Rarity fromVaries = rarityFromVaries(item.getVaries());
        return fromVaries != null ? fromVaries : DEFAULT_MAGIC_RARITY;
    }

    /** Rarity → ItemRarity VTTG (common|uncommon|rare|very-rare|legendary|artifact). */
    private String rarityCode(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> "common";
            case UNCOMMON -> "uncommon";
            case RARE -> "rare";
            case VERY_RARE -> "very-rare";
            case LEGENDARY -> "legendary";
            case ARTIFACT -> "artifact";
            // effectiveRarity() сюда VARIES/UNKNOWN не пропускает — но switch обязан быть полным.
            case VARIES, UNKNOWN -> rarityCode(DEFAULT_MAGIC_RARITY);
        };
    }

    /**
     * Минимальная редкость, упомянутая в тексте {@code varies} (напр. «редкий, очень редкий или
     * легендарный» → {@link Rarity#RARE}). Возвращает {@code null}, если ни одна редкость не распознана.
     * Порядок проверок — от младшей к старшей; учитываются перекрытия подстрок
     * («необычный» ⊃ «обычн», «очень редкий» ⊃ «редк»).
     */
    private Rarity rarityFromVaries(String varies) {
        if (!StringUtils.hasText(varies)) {
            return null;
        }
        // Убираем существительное «редкость/редкости/…», иначе его корень «редк» ложно даёт rare.
        String text = varies.toLowerCase(Locale.ROOT).replaceAll("редкост\\p{L}*", " ");
        if (text.matches(".*(?<!не)обычн.*")) {
            return Rarity.COMMON;
        }
        if (text.contains("необычн")) {
            return Rarity.UNCOMMON;
        }
        if (text.matches(".*(?<!очень )редк.*")) {
            return Rarity.RARE;
        }
        if (text.contains("очень редк")) {
            return Rarity.VERY_RARE;
        }
        if (text.contains("легендарн")) {
            return Rarity.LEGENDARY;
        }
        if (text.contains("артефакт")) {
            return Rarity.ARTIFACT;
        }
        return null;
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

    /** Выведенные из базового предмета поля: вес, стоимость в золоте и боевые/доспешные поля ({@code null} — нет). */
    private record BaseMechanics(double weight, Double costGold, Map<String, Object> fields) {
        private static final BaseMechanics EMPTY = new BaseMechanics(0, null, null);
    }
}

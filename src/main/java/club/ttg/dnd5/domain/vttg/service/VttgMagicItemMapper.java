package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import club.ttg.dnd5.domain.common.dictionary.Coin;
import club.ttg.dnd5.domain.common.dictionary.Rarity;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import club.ttg.dnd5.domain.magic.model.Attunement;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.model.MagicItemBonuses;
import club.ttg.dnd5.domain.magic.model.MagicItemCategory;
import club.ttg.dnd5.domain.magic.model.mechanics.MagicItemMechanics;
import club.ttg.dnd5.domain.magic.model.mechanics.MagicItemRechargeEvent;
import club.ttg.dnd5.domain.magic.model.mechanics.MagicItemResource;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgMagicItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    /** Ключ изменения класса доспеха в вокабуляре VTTG ({@code EffectTargetKey}). */
    private static final String ARMOR_CLASS_KEY = "armorClass";

    /** Название достроенного эффекта бонуса к КД. */
    private static final String ARMOR_CLASS_EFFECT_NAME = "Бонус к классу доспеха";

    /** Иконка достроенного эффекта — та же, что редактор ставит новым эффектам. */
    private static final String ARMOR_CLASS_EFFECT_ICON = "tabler:sparkles";

    /** Приоритет изменения по умолчанию — тот же, что проставляет редактор эффектов. */
    private static final int DEFAULT_EFFECT_PRIORITY = 20;

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
        List<Item> linked = linkedItems(item);
        if (!linked.isEmpty()) {
            // Явно связанные немагические предметы точнее уточнения (clarification): вес/стоимость берём у них.
            // Для контекстов, где нужна одна запись, берём первый из раскрытых вариантов.
            return variantsFromLinked(item, linked).get(0);
        }
        return map(item, item.getName(), item.getClarification(), item.getUrl(), item.getEnglish(), baseCache);
    }

    /**
     * Раскрывает предмет в один или несколько экспортируемых. Предметы с варьирующейся редкостью
     * не экспортируются, поэтому производные варианты «+1/+2/+3» для них также не создаются.
     * Если в {@code clarification} перечислено
     * несколько базовых предметов (напр. «полулаты или латы»), а в названии присутствует один из них,
     * на каждый базовый предмет создаётся отдельная запись с подменой этого слова в названии
     * (напр. «Латы дварфов» → «Латы дварфов» и «Полулаты дварфов»). Иначе — ровно одна запись.
     */
    public List<VttgMagicItem> toVttgVariants(MagicItem item, Map<String, List<Item>> baseCache) {
        if (item.getRarity() == Rarity.VARIES) {
            return List.of();
        }
        List<Item> linked = linkedItems(item);
        if (!linked.isEmpty()) {
            return variantsFromLinked(item, linked);
        }
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

    /** Сборка одной записи VTTG: базовый предмет разрешается из уточнения ({@code clarification}). */
    private VttgMagicItem map(MagicItem item, String name, String clarification, String url,
                              String english, Map<String, List<Item>> baseCache) {
        Item base = findBase(item.getCategory(), baseCache, clarification, name, english);
        return build(item, name, base, url, english);
    }

    /** Сборка одной записи VTTG: редкость и бонус выводятся из самого предмета (название/varies). */
    private VttgMagicItem build(MagicItem item, String name, Item base, String url, String english) {
        return buildEntry(item, name, base, url, english,
                effectiveRarity(item), magicBonus(item, name, english));
    }

    /**
     * Бонус для VTTG: там {@code magicBonus} одним числом идёт и в атаку, и в урон, поэтому у
     * оружия берём бонус к атаке, а у остального — бонус к КД. Заданные в мастерской бонусы
     * важнее вывода из названия: разбор «+1/+2/+3» остаётся запасным вариантом для записей,
     * у которых поля не заполнены.
     */
    private Integer magicBonus(MagicItem item, String name, String english) {
        return hasExplicitBonuses(item)
                ? explicitBonus(item)
                : firstBonus(name, english, item.getClarification());
    }

    /** Хоть один бонус мастерской заполнен: разбор названия такой записи уже не нужен. */
    private boolean hasExplicitBonuses(MagicItem item) {
        MagicItemBonuses bonuses = item.getBonuses();
        return bonuses != null
                && firstNonZero(bonuses.getAttack(), bonuses.getDamage(), bonuses.getArmorClass()) != 0;
    }

    /** Бонус из полей мастерской; {@code null} — поля пустые или нужного бонуса нет. */
    private Integer explicitBonus(MagicItem item) {
        MagicItemBonuses bonuses = item.getBonuses();
        if (bonuses == null) {
            return null;
        }
        int value = item.getCategory() == MagicItemCategory.WEAPON
                // Бонус к урону сюда не подставляем: он одинаковый только у обычного «+N»,
                // а разницу («+1 к атаке, +3 к урону») отдаёт отдельный damageBonus.
                ? bonuses.getAttack()
                : firstNonZero(bonuses.getArmorClass(), bonuses.getAttack(), bonuses.getDamage());
        return value == 0 ? null : value;
    }

    /**
     * Надбавка к урону сверх магического бонуса. Обычному «+N» она не нужна — такой бонус
     * потребитель уже прибавил и к атаке, и к урону; сюда попадает только разница у оружия,
     * бьющего сильнее, чем попадающего. {@code null} — надбавки нет.
     *
     * <p>Считаем лишь по заполненным полям мастерской: бонус, выведенный из названия, о
     * раздельных значениях ничего не знает, и вычитание из него дало бы отрицательную чушь.</p>
     */
    private Integer damageBonus(MagicItem item, Integer magicBonus) {
        if (item.getCategory() != MagicItemCategory.WEAPON || !hasExplicitBonuses(item)) {
            return null;
        }
        int extra = item.getBonuses().getDamage() - (magicBonus == null ? 0 : magicBonus);
        return extra == 0 ? null : extra;
    }

    private int firstNonZero(int... values) {
        for (int value : values) {
            if (value != 0) {
                return value;
            }
        }
        return 0;
    }

    /** Сборка одной записи VTTG с явно заданными редкостью и бонусом (для раскрытия «+1/+2/+3»). */
    private VttgMagicItem buildEntry(MagicItem item, String name, Item base, String url,
                                     String english, Rarity rarity, Integer bonus) {
        Attunement attunement = item.getAttunement();
        boolean requiresAttunement = attunement != null && attunement.isRequires();
        String sourceKey = VttgSourceKeys.of(item.getSource());
        MagicItemCategory category = item.getCategory();
        boolean weapon = category == MagicItemCategory.WEAPON;
        BaseMechanics mechanics = mechanics(item, base);

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
                // Страница-источник у всех записей раскрытия одна — родительская: варианты
                // («полулаты или латы», «+1/+2/+3») своих страниц на сайте не имеют.
                .srcSection(SectionType.MAGIC_ITEM.getValue())
                .srcUrl(item.getUrl())
                .srcVariant(Objects.equals(url, item.getUrl()) ? null : Boolean.TRUE)
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
                // Фокусировка — либо свойство самой записи, либо унаследованное от базы:
                // магический посох на основе боевого посоха фокусировкой быть не перестаёт.
                .isFocus(item.isFocus() || mechanics.focus())
                .isAdamantine(item.isAdamantine())
                .magicAttunement(requiresAttunement ? "required" : "none")
                .magicBonus(bonus)
                .damageBonus(damageBonus(item, bonus))
                // Механика влияния на лист персонажа: эффекты идут как есть (модель уже
                // в вокабуляре VTTG), заряды — своим блоком, пассивные свойства — текстом
                .activeEffects(activeEffects(item))
                .uses(uses(item))
                .passive(passive(item))
                .sourceKey(sourceKey)
                .isSRD(item.getSrdVersion() != null)
                .isReadOnly(true)
                .build();
    }

    /**
     * Активные эффекты предмета. Отдаются без преобразования: {@link ActiveEffect}
     * заполняется в мастерской сразу в вокабуляре VTTG — ключи изменений там
     * {@code armorClass}, {@code save.constitution}, {@code movement.swim} и прочие
     * {@code EffectTargetKey}.
     */
    private List<ActiveEffect> activeEffects(MagicItem item) {
        MagicItemMechanics mechanics = item.getMechanics();
        List<ActiveEffect> effects = mechanics == null || mechanics.getActiveEffects() == null
                ? List.of()
                : mechanics.getActiveEffects();
        ActiveEffect armorClass = armorClassEffect(item, effects);
        if (armorClass == null) {
            return effects.isEmpty() ? null : effects;
        }
        List<ActiveEffect> result = new ArrayList<>(effects);
        result.add(armorClass);
        return result;
    }

    /**
     * Бонус к КД предмета, который бронёй не является (плащ и кольцо защиты), эффектом.
     * У потребителя {@code magicBonus} поднимает класс доспеха только у самой брони и щита,
     * поэтому кольцу он ничего не даёт — такой бонус там описывают изменением
     * {@code armorClass}, и здесь оно достраивается за автора.
     *
     * <p>{@code null} — бонуса нет, категория его выражает сама (доспех, оружие) либо
     * изменение с этим ключом автор уже завёл руками: второе слагаемое подняло бы КД дважды.</p>
     */
    private ActiveEffect armorClassEffect(MagicItem item, List<ActiveEffect> effects) {
        MagicItemCategory category = item.getCategory();
        MagicItemBonuses bonuses = item.getBonuses();
        if (category == MagicItemCategory.ARMOR || category == MagicItemCategory.WEAPON
                || bonuses == null || bonuses.getArmorClass() == 0) {
            return null;
        }
        boolean described = effects.stream()
                .filter(effect -> effect.getChanges() != null)
                .flatMap(effect -> effect.getChanges().stream())
                .anyMatch(change -> ARMOR_CLASS_KEY.equals(change.getKey()));
        if (described) {
            return null;
        }
        ActiveEffect.Change change = new ActiveEffect.Change();
        change.setKey(ARMOR_CLASS_KEY);
        change.setMode("add");
        change.setValue(String.valueOf(bonuses.getArmorClass()));
        change.setPriority(DEFAULT_EFFECT_PRIORITY);

        ActiveEffect.Duration duration = new ActiveEffect.Duration();
        duration.setType("permanent");

        // Заполняем и то, что кажется необязательным: у потребителя `flags`,
        // `duration`, `disabled` и `transfer` обязательны (по ним он идёт циклом
        // и сравнением), и эффект без них уронил бы сборку эффектов надетого.
        ActiveEffect effect = new ActiveEffect();
        effect.setId(item.getUrl() + "-armor-class");
        effect.setName(ARMOR_CLASS_EFFECT_NAME);
        effect.setDescription("");
        effect.setIcon(ARMOR_CLASS_EFFECT_ICON);
        effect.setDisabled(Boolean.FALSE);
        effect.setOrigin("item");
        effect.setTransfer(Boolean.TRUE);
        effect.setDuration(duration);
        effect.setChanges(List.of(change));
        effect.setFlags(List.of());
        effect.setEffectTarget("self");
        return effect;
    }

    /**
     * Заряды предмета. {@code current} равен максимуму: справочник описывает предмет, а
     * не конкретный экземпляр — остаток живёт на листе персонажа.
     *
     * <p>Расход применения выводится только когда он больше одного: единица — значение
     * по умолчанию у потребителя, и повторять её у каждого предмета незачем.</p>
     */
    private VttgMagicItem.Uses uses(MagicItem item) {
        MagicItemMechanics mechanics = item.getMechanics();
        if (mechanics == null || mechanics.getResource() == null) {
            return null;
        }
        MagicItemResource resource = mechanics.getResource();
        Integer max = resource.getMaxCharges();
        if (max == null || max < 1) {
            return null;
        }
        Integer cost = resource.getCost();
        return new VttgMagicItem.Uses(max, max,
                rechargeEvent(resource.getRechargeEvent()),
                StringUtils.hasText(resource.getRecharge()) ? resource.getRecharge().trim() : null,
                cost != null && cost > 1 ? cost : null);
    }

    /**
     * Событие возврата зарядов в словаре потребителя. Не задано при заданном максимуме —
     * заряды возвращает только мастер вручную.
     */
    private String rechargeEvent(MagicItemRechargeEvent event) {
        if (event == null) {
            return "manual";
        }
        return switch (event) {
            case DAWN -> "dawn";
            case SHORT_REST -> "shortRest";
            case LONG_REST -> "longRest";
        };
    }

    /** Пассивные свойства предмета текстом; разметка снимается, как у описания. */
    private String passive(MagicItem item) {
        MagicItemMechanics mechanics = item.getMechanics();
        if (mechanics == null || !StringUtils.hasText(mechanics.getPassive())) {
            return null;
        }
        return markupConverter.toText(mechanics.getPassive());
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

    /**
     * Боевые/доспешные поля и вес/стоимость базового предмета плюс собственный урон магии;
     * {@code EMPTY} — базы нет и добавлять магии нечего.
     */
    private BaseMechanics mechanics(MagicItem item, Item base) {
        List<Map<String, Object>> ownParts = ownDamageParts(item);
        if (base == null) {
            // Без базы остаются только собственные части магии: потерять их молча хуже,
            // чем отдать запись без основного броска — уточнение всё равно не разрешилось.
            return ownParts.isEmpty()
                    ? BaseMechanics.EMPTY
                    : new BaseMechanics(0, null, false, Map.of("damageParts", ownParts));
        }
        MagicItemCategory category = item.getCategory();
        Map<String, Object> baseMap = itemMapper.toVttg(base);
        Map<String, Object> fields = new LinkedHashMap<>();
        // Боевые/доспешные поля имеют смысл только для оружия/брони; для прочих категорий берём лишь вес/стоимость.
        List<String> keys = switch (category == null ? MagicItemCategory.SUBJECT : category) {
            case WEAPON -> WEAPON_KEYS;
            case ARMOR -> ARMOR_KEYS;
            default -> List.of();
        };
        for (String key : keys) {
            if (baseMap.containsKey(key)) {
                fields.put(key, baseMap.get(key));
            }
        }
        if (!ownParts.isEmpty()) {
            // Урон магии идёт после броска основы: «2к6 огнём» Огненного языка — добавка
            // к рубящему урону длинного меча, а не замена ему.
            List<Map<String, Object>> parts = new ArrayList<>(baseDamageParts(baseMap));
            parts.addAll(ownParts);
            fields.put("damageParts", parts);
        }
        double weight = baseMap.get("weight") instanceof Number number ? number.doubleValue() : 0;
        return new BaseMechanics(weight, goldCost(base), Boolean.TRUE.equals(baseMap.get("isFocus")),
                fields.isEmpty() ? null : fields);
    }

    /**
     * Собственные части урона магического предмета в формате VTTG. Незаполненные строки
     * формы отбрасываются: пустая часть — обычное состояние редактора, а не значение.
     */
    private List<Map<String, Object>> ownDamageParts(MagicItem item) {
        if (CollectionUtils.isEmpty(item.getDamageParts())) {
            return List.of();
        }
        return item.getDamageParts().stream()
                .filter(part -> part != null && StringUtils.hasText(part.getFormula()))
                .map(itemMapper::damagePart)
                .toList();
    }

    /**
     * Части урона базового предмета из его же выгрузки. Читаем через {@code instanceof},
     * а не приведением типа: значение пришло из карты {@code Object}, и непроверенное
     * приведение сломалось бы молча, попадись там что-то другое.
     */
    private List<Map<String, Object>> baseDamageParts(Map<String, Object> baseMap) {
        if (!(baseMap.get("damageParts") instanceof List<?> parts)) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>(parts.size());
        for (Object part : parts) {
            if (part instanceof Map<?, ?> map) {
                Map<String, Object> copy = new LinkedHashMap<>();
                map.forEach((key, value) -> copy.put(String.valueOf(key), value));
                result.add(copy);
            }
        }
        return result;
    }

    /** Связанные немагические предметы магического предмета в стабильном порядке (по url); пусто — связей нет. */
    private List<Item> linkedItems(MagicItem item) {
        Set<Item> items = item.getItems();
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
                .sorted(Comparator.comparing(Item::getUrl, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    /**
     * Раскрывает магический предмет по явно связанным немагическим предметам:
     * <ul>
     *   <li><b>Шаблон «+1, +2 или +3»</b> (Доспех/Щит/Оружие …) — на каждый связанный предмет
     *       по три записи с бонусами +1/+2/+3 (см. {@link #bonusVariants});</li>
     *   <li><b>один связанный</b> — одна запись: вес из связанного, стоимость = стоимость связанного
     *       + цена по редкости (таблица «Редкость и цена»);</li>
     *   <li><b>несколько связанных</b> — по записи на каждый связанный предмет, с подменой слова
     *       в названии (напр. «Эльфийская кольчуга» → «Эльфийская кольчуга» и
     *       «Эльфийская кольчужная рубаха»).</li>
     * </ul>
     */
    private List<VttgMagicItem> variantsFromLinked(MagicItem item, List<Item> linked) {
        if (isBonusTemplate(item)) {
            return bonusVariants(item, linked);
        }
        if (linked.size() == 1) {
            return List.of(build(item, item.getName(), linked.get(0), item.getUrl(), item.getEnglish()));
        }
        String anchor = anchor(item.getName(), linked.stream().map(Item::getName).toList());
        List<VttgMagicItem> result = new ArrayList<>(linked.size());
        for (Item base : linked) {
            if (anchor != null && base.getName() != null && base.getName().equalsIgnoreCase(anchor)) {
                // База, совпадающая с названием — исходные имя/url/english (контракт не меняется).
                result.add(build(item, item.getName(), base, item.getUrl(), item.getEnglish()));
            } else {
                String name = anchor != null
                        ? replaceAnchor(item.getName(), anchor, base.getName())
                        : item.getName() + " (" + base.getName() + ")";
                result.add(build(item, name, base, variantUrlForLinked(item, base), null));
            }
        }
        return result;
    }

    /**
     * Шаблонный «сборный» магический предмет вида «… +1, +2 или +3» (Доспех, Щит, Оружие и т.п.):
     * при экспорте раскрывается в конкретные предметы по связанным немагическим предметам.
     */
    private boolean isBonusTemplate(MagicItem item) {
        return item.getName() != null && item.getName().contains("+1, +2 или +3");
    }

    /**
     * Раскрытие шаблона «+1, +2 или +3»: на каждый связанный немагический предмет — три записи
     * (Кожаный доспех +1/+2/+3, …, Латы +1/+2/+3). Вес берётся из базы, стоимость = стоимость базы
     * + цена по редкости соответствующего бонуса, редкость/бонус выставляются явно.
     */
    private List<VttgMagicItem> bonusVariants(MagicItem item, List<Item> linked) {
        List<VttgMagicItem> result = new ArrayList<>(linked.size() * 3);
        for (Item base : linked) {
            for (int bonus = 1; bonus <= 3; bonus++) {
                String name = base.getName() + " +" + bonus;
                String english = StringUtils.hasText(base.getEnglish())
                        ? base.getEnglish() + " +" + bonus
                        : null;
                String url = variantUrlForLinked(item, base) + "-plus-" + bonus;
                result.add(buildEntry(item, name, base, url, english, rarityForBonus(base, bonus), bonus));
            }
        }
        return result;
    }

    /**
     * Редкость по величине бонуса. Оружие и щиты дешевле брони на один уровень:
     * оружие/щит +1/+2/+3 → необычный/редкий/очень редкий; броня +1/+2/+3 → редкий/очень редкий/легендарный.
     */
    private Rarity rarityForBonus(Item base, int bonus) {
        boolean weaponTable = base.getWeapon() != null
                || (base.getArmor() != null && base.getArmor().getCategory() == ArmorCategory.SHIELD);
        if (weaponTable) {
            return switch (bonus) {
                case 1 -> Rarity.UNCOMMON;
                case 2 -> Rarity.RARE;
                default -> Rarity.VERY_RARE;
            };
        }
        return switch (bonus) {
            case 1 -> Rarity.RARE;
            case 2 -> Rarity.VERY_RARE;
            default -> Rarity.LEGENDARY;
        };
    }

    /** Url дополнительного варианта по связанному предмету: url предмета + slug url базы (стабильный, уникальный). */
    private String variantUrlForLinked(MagicItem item, Item base) {
        String suffix = slug(base.getUrl());
        if (suffix.isEmpty()) {
            suffix = slug(base.getName());
        }
        return suffix.isEmpty() ? item.getUrl() : item.getUrl() + "-" + suffix;
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
        String replacement = base;
        if (!matched.isEmpty() && !base.isEmpty()) {
            // Регистр первой буквы базы выравниваем по заменяемому слову («Латы»→«Полулаты», «кольчуга»→«кольчужная рубаха»).
            replacement = Character.isUpperCase(matched.charAt(0))
                    ? Character.toUpperCase(base.charAt(0)) + base.substring(1)
                    : Character.toLowerCase(base.charAt(0)) + base.substring(1);
        }
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


    /**
     * Выведенные из базового предмета поля: вес, стоимость в золоте, признак заклинательной
     * фокусировки и боевые/доспешные поля ({@code null} — нет).
     */
    private record BaseMechanics(double weight, Double costGold, boolean focus, Map<String, Object> fields) {
        private static final BaseMechanics EMPTY = new BaseMechanics(0, null, false, null);
    }
}

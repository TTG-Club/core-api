package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Language;
import club.ttg.dnd5.domain.common.dictionary.SenseType;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.model.mechanics.ResourceRecovery;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Перевод словарей источника в ключи компендиума VTTG.
 *
 * <p>Одни и те же enum'ы переводят несколько мапперов (существа, класс, предыстория,
 * черта), и переводы успели разойтись по копиям. Здесь они лежат один раз — новый
 * маппер берёт их отсюда, а старые переезжают по мере правок, чтобы не тащить чужие
 * изменения в один заход.</p>
 *
 * <p>Общее правило: ключ VTTG — это имя enum'а в нижнем регистре. Исключения перечислены
 * явно и снабжены причиной; каждое из них — расхождение словарей, а не стиль.</p>
 */
final class VttgDictionaries {

    private VttgDictionaries() {
    }

    /** Характеристика: {@code STRENGTH → "strength"}. */
    static String ability(Ability ability) {
        return ability == null ? null : ability.name().toLowerCase(Locale.ROOT);
    }

    static List<String> abilities(Collection<Ability> abilities) {
        return map(abilities, VttgDictionaries::ability);
    }

    /**
     * Навык в camelCase: {@code SLEIGHT_OF_HAND → "sleightOfHand"}. Составные имена у
     * потребителя пишутся именно так — не через дефис.
     */
    static String skill(Skill skill) {
        if (skill == null) {
            return null;
        }
        String[] parts = skill.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder result = new StringBuilder(parts[0]);
        for (int index = 1; index < parts.length; index++) {
            String part = parts[index];
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return result.toString();
    }

    static List<String> skills(Collection<Skill> skills) {
        return map(skills, VttgDictionaries::skill);
    }

    /** Категория доспеха: {@code MEDIUM → "medium"}. */
    static String armorCategory(ArmorCategory category) {
        return category == null ? null : category.name().toLowerCase(Locale.ROOT);
    }

    static List<String> armorCategories(Collection<ArmorCategory> categories) {
        return map(categories, VttgDictionaries::armorCategory);
    }

    /**
     * Категория оружия. Словарь источника делит категории ещё и по дальнобойности
     * ({@code MATERIAL_MELEE}/{@code MATERIAL_RANGED}), а правила — нет: у потребителя
     * это простое и воинское оружие.
     */
    static String weaponCategory(WeaponCategory category) {
        if (category == null) {
            return null;
        }
        return category.name().startsWith("SIMPLE") ? "simple" : "martial";
    }

    static List<String> weaponCategories(Collection<WeaponCategory> categories) {
        return map(categories, VttgDictionaries::weaponCategory).stream().distinct().toList();
    }

    /** Тип урона. */
    static String damageType(DamageType type) {
        if (type == null) {
            return null;
        }
        return type.name().toLowerCase(Locale.ROOT);
    }

    static List<String> damageTypes(Collection<DamageType> types) {
        return map(types, VttgDictionaries::damageType);
    }

    /**
     * Тип урона, записанный строкой (значение варианта выбора у черты).
     *
     * <p>{@code FAIR} принимается наравне с {@code FIRE}: это историческая опечатка имени
     * константы, и в сохранённых данных она ещё встречается — тот же алиас стоит и на самом
     * {@link DamageType}.</p>
     */
    static DamageType damageTypeValue(String raw) {
        DamageType type = enumValue(DamageType.class, raw);
        if (type != null) {
            return type;
        }
        return raw != null && "FAIR".equalsIgnoreCase(raw.trim()) ? DamageType.FIRE : null;
    }

    /** Состояние: {@code POISONED → "poisoned"}. */
    static String condition(Condition condition) {
        return condition == null ? null : condition.name().toLowerCase(Locale.ROOT);
    }

    static List<String> conditions(Collection<Condition> conditions) {
        return map(conditions, VttgDictionaries::condition);
    }

    /**
     * Чувство: {@code BLINDSIGHT → "blindsight"}. Тёмное зрение у потребителя живёт
     * своим полем (зрение токена), поэтому в общий список чувств не идёт.
     */
    static String sense(SenseType sense) {
        if (sense == null || sense == SenseType.DARKVISION) {
            return null;
        }
        return sense.name().toLowerCase(Locale.ROOT);
    }

    /**
     * Язык — РУССКИМ НАЗВАНИЕМ, а не ключом: у потребителя языки хранятся подписями из
     * своего справочника ({@code proficiencies.languages}), ключей у них нет вовсе.
     *
     * <p>Единственный словарь, который нельзя вывести правилом: названия расходятся почти
     * везде («дварфский» против «Дварфийский», «бездны» против «Абиссальный», «первичный»
     * против «Первоязык»). Группировка (распространённый/редкий) не переносится: у листа
     * она своя и зашита порядком справочника.</p>
     *
     * @return название языка для листа
     */
    static String language(Language language) {
        if (language == null) {
            return null;
        }
        return switch (language) {
            case COMMON -> "Общий";
            case DWARVISH -> "Дварфийский";
            case ELVISH -> "Эльфийский";
            case GIANT -> "Гигантский";
            case GNOMISH -> "Гномский";
            case GOBLIN -> "Гоблинский";
            case HALFLING -> "Полуросликовский";
            case ORC -> "Оркский";
            case ABYSSAL -> "Абиссальный";
            case Celestial -> "Небесный";
            case DEEP -> "Глубинная речь";
            case DRACONIC -> "Драконий";
            case INFERNAL -> "Инфернальный";
            case PRIMORDIAL -> "Первоязык";
            case SYLVAN -> "Сильван";
            case UNDERCOMMON -> "Подземный";
            case DRUIDIC -> "Друидический";
            case THIEVES -> "Язык воров";
            // Пары в справочнике листа нет, но список языков там открытый: незнакомое
            // название лист показывает и сохраняет как «свой язык», поэтому владение
            // доезжает, а не пропадает
            case COMMON_SIGN_LANGUAGE -> "Общий язык жестов";
        };
    }

    static List<String> languages(Collection<Language> languages) {
        return map(languages, VttgDictionaries::language);
    }

    /**
     * Откат ресурса: {@code LONG_REST → "long"}, {@code SHORT_REST_ONE → "short-one"}.
     * Правилом не выводится — у потребителя это строка словаря, и тот же перевод делает
     * выгрузка класса.
     */
    static String recovery(ResourceRecovery recovery) {
        if (recovery == ResourceRecovery.SHORT_REST) {
            return "short";
        }
        return recovery == ResourceRecovery.SHORT_REST_ONE ? "short-one" : "long";
    }

    /**
     * Значение словаря, записанное строкой, — в свой enum.
     *
     * <p>Строки приходят из JSONB, набранного в редакторе, и валидации у них нет: одно
     * кривое значение не должно ронять выгрузку всего компендиума, поэтому вместо
     * {@code valueOf} здесь мягкий поиск. Регистр игнорируется намеренно — константа
     * {@code Language.Celestial} записана не капсом, и точное сравнение спотыкается
     * именно на ней.</p>
     *
     * @return значение enum'а либо {@code null}, если такого нет
     */
    static <E extends Enum<E>> E enumValue(Class<E> type, String raw) {
        if (type == null || !StringUtils.hasText(raw)) {
            return null;
        }
        String name = raw.trim();
        for (E value : type.getEnumConstants()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    /** Общий проход: {@code null}-значения и непереводимые элементы отбрасываются. */
    private static <T> List<String> map(Collection<T> values,
                                        java.util.function.Function<T, String> mapper) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .map(mapper)
                .filter(Objects::nonNull)
                .toList();
    }
}

package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.SenseType;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;

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

    /**
     * Тип урона. {@code FAIR} — опечатка в словаре источника: это огонь, и у потребителя
     * он называется {@code fire}.
     */
    static String damageType(DamageType type) {
        if (type == null) {
            return null;
        }
        return type == DamageType.FAIR ? "fire" : type.name().toLowerCase(Locale.ROOT);
    }

    static List<String> damageTypes(Collection<DamageType> types) {
        return map(types, VttgDictionaries::damageType);
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

package club.ttg.dnd5.domain.vttg.service;

import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Set;

/**
 * Ключ вида оружия в вокабуляре VTTG.
 *
 * <p>Нужен там, где черта даёт выбрать конкретное оружие («Мастер оружия»): в модели такой
 * вариант записан ссылкой на предмет ({@code longsword-phb}), а владение на листе сверяется
 * с базовым типом оружия ({@code longsword}) — со слагом страницы оно не сойдётся.</p>
 *
 * <p>Словарь перечислен целиком: набор базовых видов оружия закрыт правилами, а выдавать
 * ключ, которого у листа нет, бессмысленно — проверка владения по нему никогда не сработает.
 * Категории ({@code simple}/{@code martial}) сюда не входят: они переводятся из своего
 * enum'а в {@link VttgDictionaries#weaponCategory}.</p>
 */
final class VttgWeaponKeys {
    /** Базовые виды оружия справочника листа. */
    private static final Set<String> KEYS = Set.of(
            // Простое рукопашное
            "club", "dagger", "greatclub", "handaxe", "javelin", "light-hammer", "mace",
            "quarterstaff", "sickle", "spear",
            // Простое дальнобойное
            "dart", "shortbow", "light-crossbow", "sling",
            // Воинское рукопашное
            "battleaxe", "flail", "glaive", "greataxe", "greatsword", "halberd", "lance",
            "longsword", "maul", "morningstar", "pike", "rapier", "scimitar", "shortsword",
            "trident", "war-pick", "warhammer", "whip",
            // Воинское дальнобойное
            "blowgun", "hand-crossbow", "heavy-crossbow", "longbow", "musket", "pistol"
    );

    /**
     * Сколько хвостовых сегментов разрешено отбросить: ровно один — суффикс источника.
     * Со вторым {@code sling-bullet-phb} («пули для пращи») превратился бы во владение
     * пращой.
     */
    private static final int MAX_DROPPED_SEGMENTS = 1;

    private VttgWeaponKeys() {
    }

    /**
     * Ключ вида оружия по слагу страницы: {@code "longsword-phb" → "longsword"}.
     *
     * @return ключ справочника листа либо {@code null}, если такого оружия у листа нет
     */
    static String ofUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        String candidate = url.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        for (int dropped = 0; dropped <= MAX_DROPPED_SEGMENTS; dropped++) {
            if (KEYS.contains(candidate)) {
                return candidate;
            }
            int lastDash = candidate.lastIndexOf('-');
            if (lastDash < 0) {
                return null;
            }
            candidate = candidate.substring(0, lastDash);
        }
        return null;
    }
}

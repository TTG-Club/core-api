package club.ttg.dnd5.domain.vttg.service;

import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Set;

/**
 * Ключ базового доспеха в вокабуляре VTTG.
 *
 * <p>Лист персонажа хранит владение доспехом ключом своего справочника ({@code leather}), а
 * сайт называет доспех как в книге: «Leather Armor», страница {@code leather-armor-phb}.
 * Слаг английского названия даёт {@code leather-armor}, которого у листа нет, — и доспех,
 * которым персонаж владеет, лист считает чужим: помеха на проверки Силы и Ловкости и на
 * атаки. Так расходятся 7 доспехов из 13 — все, у кого в названии есть слово «Armor».</p>
 *
 * <p>Словарь перечислен целиком: набор базовых доспехов закрыт правилами, а ключ, которого
 * у листа нет, бессмысленно выдавать — владение по нему не сработает.</p>
 */
final class VttgArmorKeys {
    /** Базовые доспехи справочника листа. */
    private static final Set<String> KEYS = Set.of(
            // Лёгкие
            "padded", "leather", "studded-leather",
            // Средние
            "hide", "chain-shirt", "scale-mail", "breastplate", "half-plate",
            // Тяжёлые
            "ring-mail", "chain-mail", "splint", "plate",
            // Щит
            "shield"
    );

    /** Слово, которым книга дополняет название доспеха, а справочник листа — нет. */
    private static final String ARMOR_SUFFIX = "-armor";

    /**
     * Сколько хвостовых сегментов разрешено отбросить: ровно один — суффикс источника.
     * Больше — уже угадывание.
     */
    private static final int MAX_DROPPED_SEGMENTS = 1;

    private VttgArmorKeys() {
    }

    /**
     * Ключ доспеха по слагу страницы: {@code "leather-armor-phb" → "leather"},
     * {@code "chain-mail-phb" → "chain-mail"}.
     *
     * @return ключ справочника листа либо {@code null}, если такого доспеха у листа нет
     */
    static String ofUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        String candidate = url.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        for (int dropped = 0; dropped <= MAX_DROPPED_SEGMENTS; dropped++) {
            String key = known(candidate);
            if (key != null) {
                return key;
            }
            int lastDash = candidate.lastIndexOf('-');
            if (lastDash < 0) {
                return null;
            }
            candidate = candidate.substring(0, lastDash);
        }
        return null;
    }

    private static String known(String candidate) {
        if (KEYS.contains(candidate)) {
            return candidate;
        }
        if (candidate.endsWith(ARMOR_SUFFIX)) {
            String withoutSuffix = candidate.substring(0, candidate.length() - ARMOR_SUFFIX.length());
            return KEYS.contains(withoutSuffix) ? withoutSuffix : null;
        }
        return null;
    }
}

package club.ttg.dnd5.domain.vttg.service;

import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Канонический ключ класса в вокабуляре VTTG.
 *
 * <p>Правило одно на всю выгрузку намеренно: по этому ключу сходятся список заклинаний
 * класса ({@code spell.classKeys}), запись самого класса ({@code class.key}) и фильтр
 * выбора заклинаний у черты. Разойдись эти места — пул заклинаний «Посвящённого в
 * магию» собрался бы пустым, и молча.</p>
 *
 * <p>Входов у ключа два, и они не сводятся друг к другу: у заклинания принадлежность
 * записана английским НАЗВАНИЕМ класса, а у черты — слагом СТРАНИЦЫ ({@code wizard-phb}).
 * Поэтому и методов два.</p>
 */
final class VttgClassKeys {
    /**
     * Ключи, которые понимает потребитель. Всё, что не в списке (хоумбрю-классы), ключа не
     * получает: у листа персонажа для них нет ни списка заклинаний, ни подписи.
     */
    private static final Set<String> CANONICAL = Set.of(
            "artificer", "barbarian", "bard", "cleric", "druid", "fighter",
            "monk", "paladin", "ranger", "rogue", "sorcerer", "warlock", "wizard"
    );

    private VttgClassKeys() {
    }

    /**
     * Ключ по английскому названию класса: {@code "Wizard" → "wizard"}.
     *
     * <p>Небуквенные символы выбрасываются целиком, а не заменяются дефисом: у
     * канонических классов названия односложные, и любой разделитель здесь — мусор
     * разметки, а не часть ключа.</p>
     *
     * @return ключ либо {@code null}, если класс не канонический
     */
    static String ofEnglishName(String english) {
        if (!StringUtils.hasText(english)) {
            return null;
        }
        String key = english.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        return CANONICAL.contains(key) ? key : null;
    }

    /**
     * Ключ по слагу страницы класса: {@code "wizard-phb" → "wizard"}.
     *
     * <p>Суффикс источника отсекается тем же правилом, которым потребитель уже мирит
     * ссылку с ключом: слаг либо равен ключу, либо начинается с него и дефиса. Ни один
     * канонический ключ не является началом другого, поэтому порядок перебора не важен.</p>
     *
     * @return ключ либо {@code null}, если слаг не принадлежит каноническому классу
     */
    static String ofUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        String slug = url.trim().toLowerCase(Locale.ROOT);
        for (String key : CANONICAL) {
            if (slug.equals(key) || slug.startsWith(key + "-")) {
                return key;
            }
        }
        return null;
    }

    /** Ключи по слагам страниц; неканонические слаги выпадают, дубликаты схлопываются. */
    static List<String> ofUrls(Collection<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return List.of();
        }
        return urls.stream()
                .map(VttgClassKeys::ofUrl)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}

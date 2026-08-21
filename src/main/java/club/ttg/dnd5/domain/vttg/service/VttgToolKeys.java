package club.ttg.dnd5.domain.vttg.service;

import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Ключ владения инструментом в вокабуляре VTTG.
 *
 * <p>Инструменты в TTG Club — записи раздела «Предметы», и черта ссылается на них слагом
 * страницы ({@code thieves-tools-phb}). Лист персонажа хранит владения ключами своего
 * справочника ({@code thieves-tools}) и МОЛЧА выбрасывает незнакомый ключ при первом же
 * открытии окна владений — поэтому слаг страницы отдавать нельзя, его нужно перевести.</p>
 *
 * <p>Словарь перечислен целиком, а не выведен правилом, по двум причинам. Во-первых,
 * притяжательный апостроф в слаге сайта становится отдельным сегментом
 * ({@code calligrapher-s-supplies}), а в ключе листа он просто исчезает
 * ({@code calligraphers-supplies}) — это 19 позиций из 37. Во-вторых, ключ, которого в
 * справочнике листа нет, лучше не выдавать вовсе: владение, пропавшее при следующем
 * открытии окна, хуже отсутствующего.</p>
 */
final class VttgToolKeys {
    /**
     * Ключи справочника инструментов листа персонажа: 37 конкретных плюс три обобщённые
     * группы «на выбор», которые лист принимает наравне с конкретными.
     */
    private static final Set<String> KEYS = Set.of(
            // Инструменты ремесленника
            "alchemists-supplies", "brewers-supplies", "calligraphers-supplies",
            "carpenters-tools", "cartographers-tools", "cobblers-tools", "cooks-utensils",
            "glassblowers-tools", "jewelers-tools", "leatherworkers-tools", "masons-tools",
            "painters-supplies", "potters-tools", "smiths-tools", "tinkers-tools",
            "weavers-tools", "woodcarvers-tools",
            // Игровые наборы
            "dice-set", "dragonchess-set", "playing-card-set", "three-dragon-ante-set",
            // Музыкальные инструменты
            "bagpipes", "drum", "dulcimer", "flute", "lute", "lyre", "horn", "pan-flute",
            "shawm", "viol",
            // Прочие
            "disguise-kit", "forgery-kit", "herbalism-kit", "navigators-tools",
            "poisoners-kit", "thieves-tools",
            // Обобщённая группа: у неё есть своя запись в справочнике предметов, и лист
            // узнаёт её так же, как конкретный инструмент. Двух других групп
            // («инструменты ремесленника», «игровой набор») в словаре листа нет —
            // ссылка на них остаётся ссылкой, иначе владение исчезло бы молча
            "musical-instrument"
    );

    /**
     * Слаги сайта, которые правилом не сводятся: там, где справочник листа пишет название
     * во множественном числе или с уточнением, а сайт — как в книге.
     */
    private static final Map<String, String> ALIASES = Map.of(
            "bagpipe", "bagpipes",
            "playing-cards", "playing-card-set"
    );

    /**
     * Сколько хвостовых сегментов разрешено отбросить в поисках ключа. Ровно один — это
     * суффикс источника ({@code -phb}), больше отбрасывать нечего. Второй сегмент уже
     * превращает поиск в угадывание: «пули для пращи» стали бы владением пращой.
     */
    private static final int MAX_DROPPED_SEGMENTS = 1;

    private VttgToolKeys() {
    }

    /**
     * Ключ владения по слагу страницы инструмента: {@code "thieves-tools-phb" → "thieves-tools"},
     * {@code "calligrapher-s-supplies-phb" → "calligraphers-supplies"}.
     *
     * @return ключ справочника листа либо {@code null}, если такого инструмента у листа нет
     */
    static String ofUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        String candidate = url.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "")
                // Притяжательный апостроф: «Calligrapher's Supplies» на сайте становится
                // calligrapher-s-supplies, у листа — calligraphers-supplies
                .replace("-s-", "s-");

        for (int dropped = 0; dropped <= MAX_DROPPED_SEGMENTS; dropped++) {
            if (KEYS.contains(candidate)) {
                return candidate;
            }
            String alias = ALIASES.get(candidate);
            if (alias != null) {
                return alias;
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

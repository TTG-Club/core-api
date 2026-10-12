package club.ttg.dnd5.domain.article.model;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Нормализация тегов записей: один и тот же тег, набранный по-разному («Лор», « лор »),
 * должен попадать в один фильтр.
 */
public final class ArticleTags {

    /**
     * Максимальная длина тега (длина колонки {@code article_tag.tag}).
     */
    public static final int MAX_LENGTH = 64;

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private ArticleTags() {
    }

    /**
     * Приводит теги к хранимому виду: обрезает и схлопывает пробелы, переводит в нижний регистр,
     * отбрасывает пустые, режет до {@link #MAX_LENGTH} и убирает дубли. Результат — по алфавиту.
     */
    public static Set<String> normalize(Collection<String> tags) {
        Set<String> normalized = new TreeSet<>();
        if (tags == null) {
            return normalized;
        }
        tags.stream()
                .filter(Objects::nonNull)
                .map(ArticleTags::normalize)
                .filter(tag -> !tag.isEmpty())
                .forEach(normalized::add);
        return normalized;
    }

    private static String normalize(String tag) {
        String normalized = WHITESPACE.matcher(tag.trim()).replaceAll(" ").toLowerCase(Locale.ROOT);
        return normalized.length() > MAX_LENGTH ? normalized.substring(0, MAX_LENGTH).trim() : normalized;
    }
}

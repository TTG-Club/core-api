package club.ttg.dnd5.util;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Поиск и замена адресов записей каталога ({@code fireball-phb}) в произвольном тексте:
 * в разметке описаний, в JSON, в путях сайта. Адрес ищется целым словом — граница
 * адреса любой символ, кроме латиницы, цифр и дефиса, поэтому {@code fireball-phb}
 * внутри {@code greater-fireball-phb} или {@code fireball-phb-2} не находится.
 */
public final class UrlTokens {
    private static final String URL_CHAR = "a-z0-9-";

    private UrlTokens() {
    }

    /** Любой из адресов целым словом; длинные адреса проверяются раньше коротких. */
    public static Pattern pattern(Collection<String> urls) {
        StringBuilder alternatives = new StringBuilder();
        urls.stream()
                .sorted((left, right) -> Integer.compare(right.length(), left.length()))
                .forEach(url -> {
                    if (!alternatives.isEmpty()) {
                        alternatives.append('|');
                    }
                    alternatives.append(Pattern.quote(url));
                });
        return Pattern.compile("(?<![" + URL_CHAR + "])(?:" + alternatives + ")(?![" + URL_CHAR + "])");
    }

    /**
     * Заменяет адреса по карте.
     *
     * @param text        текст
     * @param replacements адрес → чем заменить
     * @param pattern     {@link #pattern} по ключам карты
     * @return новый текст; тот же объект, если замен не было
     */
    public static String replace(String text, Map<String, String> replacements, Pattern pattern) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return text;
        }
        StringBuilder result = new StringBuilder(text.length());
        do {
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacements.get(matcher.group())));
        } while (matcher.find());
        matcher.appendTail(result);
        return result.toString();
    }
}

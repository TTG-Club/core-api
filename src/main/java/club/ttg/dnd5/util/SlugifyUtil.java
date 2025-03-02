package club.ttg.dnd5.util;

import com.github.slugify.Slugify;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SlugifyUtil {
    private final static Slugify slugify = Slugify.builder()
            .transliterator(true)
            .build();

    /**
     * Преобразование текста в url-friendly строку
     *
     * @param text исходный текст
     * @return url-friendly строка
     */
    public static String getSlug(String text) {
        return slugify.slugify(text);
    }
}

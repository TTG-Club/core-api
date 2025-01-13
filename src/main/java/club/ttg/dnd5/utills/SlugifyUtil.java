package club.ttg.dnd5.utills;

import com.github.slugify.Slugify;
import org.apache.commons.io.FilenameUtils;

/**
 * Утилита для преобразования текста в url-friendly строки
 */
public final class SlugifyUtil {
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

    /**
     * Преобразование имени файла в url-friendly строку
     *
     * @param originalFilename оригинальное название файла
     * @return url-friendly название файла
     */
    public static String getFilenameSlug(String originalFilename) {
        String filename = FilenameUtils.getBaseName(originalFilename);
        String extension = FilenameUtils.getExtension(originalFilename);

        if (extension == null) {
            return getSlug(filename);
        }

        return String.format("%s.%s", getSlug(filename), extension);
    }
}

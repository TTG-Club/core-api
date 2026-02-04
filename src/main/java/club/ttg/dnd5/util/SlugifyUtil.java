package club.ttg.dnd5.util;

import com.github.slugify.Slugify;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.apache.commons.io.FilenameUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SlugifyUtil {
    // Используем transliterator(true) для корректной обработки кириллицы
    private final static Slugify slugify = Slugify.builder()
            .transliterator(true)
            .underscoreSeparator(false) // Принудительно используем дефисы
            .build();

    /**
     * Преобразование текста в url-friendly строку (базовый метод)
     *
     * @param text исходный текст (например, заголовок статьи)
     * @return url-friendly строка
     */
    public static String getSlug(String text) {
        return slugify.slugify(text);
    }

    /**
     * Генерирует безопасное имя файла для S3.
     * Слагифицирует только имя файла, сохраняя расширение.
     *
     * @param originalFilename исходное имя файла (например, "Мой Аватар.png")
     * @return безопасное имя файла (например, "moj-avatar.png")
     */
    public static String getFileName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "unnamed-file";
        }

        // Отделяем расширение надежным способом
        String extension = FilenameUtils.getExtension(originalFilename);
        String nameWithoutExtension = FilenameUtils.getBaseName(originalFilename);

        // Слагифицируем имя
        String slugName = slugify.slugify(nameWithoutExtension);

        // Если расширения нет, возвращаем просто слаг
        if (extension.isEmpty()) {
            return slugName;
        }

        // Собираем обратно: имя-слаг.расширение
        return slugName + "." + extension.toLowerCase();
    }
}

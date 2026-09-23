package club.ttg.dnd5.util;

import lombok.experimental.UtilityClass;

/**
 * Уравнивает «ё» и «е» в текстовом поиске.
 * <p>
 * В названиях буква пишется непоследовательно («Посвящённый в магию», но
 * «Посвященный Зеленых певцов»), и пользователь тоже вводит её как придётся.
 * Поэтому «ё» заменяется на «е» с обеих сторон сравнения: в строке запроса —
 * здесь, в колонке — выражением {@link #SQL_TEMPLATE} (или тем же
 * {@code replace(replace(...))} прямо в HQL-запросах репозиториев).
 */
@UtilityClass
public class YoUtils
{
    /**
     * QueryDSL-шаблон, заменяющий «ё» на «е» в колонке {@code {0}}.
     */
    public static final String SQL_TEMPLATE = "replace(replace({0}, 'Ё', 'Е'), 'ё', 'е')";

    public static String replaceYo(final String line)
    {
        return line == null ? null : line.replace('Ё', 'Е').replace('ё', 'е');
    }
}

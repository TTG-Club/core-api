package club.ttg.dnd5.dto.base.filters;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Базовый класс для всех поисковых запросов.
 * Содержит общие параметры: текстовый поиск, 2-state источники, пагинацию.
 */
@Data
@NoArgsConstructor
public abstract class AbstractSearchRequest
{
    /**
     * Строка текстового поиска (по name, english, alternative).
     */
    private String text;

    /**
     * 2-state фильтр источников: множество включённых акронимов.
     * Если {@code null} или пустое — фильтрация по источникам не применяется.
     */
    private Set<String> enabledSources;

    /**
     * Номер страницы (0-based).
     */
    private int page = 0;

    /**
     * Размер страницы.
     */
    private int size = 50;
}

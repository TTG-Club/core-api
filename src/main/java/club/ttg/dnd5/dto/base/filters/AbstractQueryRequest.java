package club.ttg.dnd5.dto.base.filters;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Базовый класс для всех поисковых запросов (API v2).
 * Содержит общие параметры: текстовый поиск, источники, пагинацию.
 */
@Data
@NoArgsConstructor
public abstract class AbstractQueryRequest
{
    public static final int DEFAULT_PAGE_SIZE = 100;
    public static final int MAX_PAGE_SIZE = 200;

    /** Строка текстового поиска (по name, english, alternative). */
    private String search;

    /** 2-state фильтр источников: множество включённых акронимов. */
    private Set<String> source = Set.of();

    /** Номер страницы (0-based). */
    private int page = 0;

    /** Размер страницы. */
    private int pageSize = DEFAULT_PAGE_SIZE;

    public void setPage(int page)
    {
        this.page = Math.max(0, page);
    }

    public void setPageSize(int pageSize)
    {
        if (pageSize <= 0)
        {
            this.pageSize = DEFAULT_PAGE_SIZE;
            return;
        }

        this.pageSize = Math.min(pageSize, MAX_PAGE_SIZE);
    }
}

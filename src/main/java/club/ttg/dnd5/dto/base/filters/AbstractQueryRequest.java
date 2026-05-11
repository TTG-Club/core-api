package club.ttg.dnd5.dto.base.filters;

import club.ttg.dnd5.domain.filter.rest.FilterParam;
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
    /** Строка текстового поиска (по name, english, alternative). */
    private String search;

    /** 2-state фильтр источников: множество включённых акронимов. */
    private Set<String> source = Set.of();

    /** Фильтр по версии SRD, например "5.1". */
    @FilterParam
    private QueryFilter<String> srdVersion;

    /** Номер страницы (0-based). */
    private int page = 0;

    /** Размер страницы. */
    private int pageSize = 10000;
}

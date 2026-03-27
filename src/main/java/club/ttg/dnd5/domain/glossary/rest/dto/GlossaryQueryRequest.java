package club.ttg.dnd5.domain.glossary.rest.dto;

import club.ttg.dnd5.dto.base.filters.QueryFilter;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GlossaryQueryRequest
{
    private String search;
    private QueryFilter<String> tagCategory;
    private int page = 0;
    private int pageSize = 10000;
}

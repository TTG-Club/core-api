package club.ttg.dnd5.domain.glossary.rest.dto;

import club.ttg.dnd5.domain.filter.rest.FilterParam;
import club.ttg.dnd5.dto.base.filters.AbstractQueryRequest;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GlossaryQueryRequest extends AbstractQueryRequest
{
    @FilterParam
    private QueryFilter<String> tagCategory;
}

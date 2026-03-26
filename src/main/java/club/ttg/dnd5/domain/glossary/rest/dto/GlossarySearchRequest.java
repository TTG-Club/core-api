package club.ttg.dnd5.domain.glossary.rest.dto;

import club.ttg.dnd5.dto.base.filters.AbstractSearchRequest;
import club.ttg.dnd5.dto.base.filters.ThreeStateFilter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO запроса фильтрации глоссария.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GlossarySearchRequest extends AbstractSearchRequest
{
    /** Категория тега (3-state по строковым значениям). */
    private ThreeStateFilter<String> tagCategory;
}

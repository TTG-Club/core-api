package club.ttg.dnd5.domain.filter.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Объектная структура поддерживаемых фич группы фильтров.
 * Заменяет плоские {@code supportsMode} / {@code supportsUnion}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportsConfig
{
    private boolean mode;
    private boolean union;
}

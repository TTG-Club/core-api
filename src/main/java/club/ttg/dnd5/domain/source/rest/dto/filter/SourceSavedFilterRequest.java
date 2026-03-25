package club.ttg.dnd5.domain.source.rest.dto.filter;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SourceSavedFilterRequest {
    private UUID id;
    private FilterInfo filter;
}

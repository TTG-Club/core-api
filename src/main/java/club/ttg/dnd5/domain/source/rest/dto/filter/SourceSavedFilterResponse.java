package club.ttg.dnd5.domain.source.rest.dto.filter;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class SourceSavedFilterResponse {
    private UUID id;
    private FilterInfo filterInfo;
}

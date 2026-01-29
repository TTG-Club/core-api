package club.ttg.dnd5.domain.common.rest.dto.container;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MetadataResponse {
    private PaginationResponse pagination;
}

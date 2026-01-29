package club.ttg.dnd5.domain.common.rest.dto.container;

import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class PaginationResponse {
    private long total;
    private long limit;
    private long skip;
}

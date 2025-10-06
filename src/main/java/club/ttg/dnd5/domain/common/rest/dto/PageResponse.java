package club.ttg.dnd5.domain.common.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.Collection;

@Builder
@Getter
public class PageResponse <T> {
    @Schema(description = "Элементы")
    private Collection<T> items;
    @Schema(description = "Пагинация")
    private Pagination pagination;
}

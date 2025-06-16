package club.ttg.dnd5.domain.beastiary.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class LegendaryActionResponse {
    @Schema(description = "Легендарные действия")
    private Collection<ActionResponse> actions;
    @Schema(description ="Количество легендарных действий")
    private String count;
    @Schema(description ="Описание использования легендарных действий (если отличается от стандартного)")
    private String description;
}

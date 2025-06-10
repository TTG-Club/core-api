package club.ttg.dnd5.domain.beastiary.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class LegendaryActionRequest {
    @Schema(description = "Легендарные действия")
    private Collection<ActionRequest> actions;
    @Schema(description ="Количество легендарных действий")
    private Byte count;
    @Schema(description ="Количество легендарных действий в логове")
    private Byte inLair;
    @Schema(description ="Описание легендарных действий (если отличается от стандартного)")
    private String description;
}

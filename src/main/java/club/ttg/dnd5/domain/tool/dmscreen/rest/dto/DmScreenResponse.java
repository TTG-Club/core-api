package club.ttg.dnd5.domain.tool.dmscreen.rest.dto;

import club.ttg.dnd5.domain.tool.dmscreen.model.DmScreenWindow;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DmScreenResponse {

    @NotNull
    @Schema(description = "Окна ширмы в сохранённом порядке; пустой список — ширму ещё не сохраняли")
    private List<DmScreenWindow> windows;

    @Nullable
    @Schema(description = "Дата последнего сохранения; null — ширму ещё не сохраняли")
    private Instant updatedAt;
}

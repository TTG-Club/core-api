package club.ttg.dnd5.domain.tool.tracker.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TrackerRequest {

    @Nullable
    @Size(max = 100)
    @Schema(description = "Название трекера (по умолчанию — «Новый трекер»)")
    private String name;

    @Nullable
    @Schema(description = "Опция «новая инициатива каждый раунд»: true — перебрасывать инициативу всем "
            + "живым в начале каждого раунда. При обновлении null — не менять; при создании по умолчанию false")
    private Boolean rerollEachRound;
}

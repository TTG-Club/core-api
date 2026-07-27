package club.ttg.dnd5.domain.statistics.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CharacterSheetStatisticsResponse {

    @Schema(description = "Все листы персонажей сайта: активные и лежащие в истории удалённых")
    private long total;

    @Schema(description = "Активные (неудалённые) листы персонажей")
    private long active;
}

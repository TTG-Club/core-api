package club.ttg.dnd5.domain.beastiary.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatureDefenses {
    @Schema(description = "Уязвимости")
    private CreatureDefense vulnerabilities;
    @Schema(description = "Сопротивления")
    private CreatureDefense resistances;
    @Schema(description = "Иммунитеты")
    private CreatureImmunities immunities;
}

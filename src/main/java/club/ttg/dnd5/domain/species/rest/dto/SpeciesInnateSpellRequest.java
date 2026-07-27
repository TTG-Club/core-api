package club.ttg.dnd5.domain.species.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpeciesInnateSpellRequest
{
    @Schema(description = "URL заклинания")
    private String spell;

    @Schema(description = "Уровень персонажа, с которого доступно заклинание")
    private Integer requiredLevel;
}

package club.ttg.dnd5.dto.bestiary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Уровень опасности")
public class ChallengeRatingDto {
    @Schema(description = "Значение УО")
    private String cr;
    @Schema(description = "Опыт за победу")
    private long experience;
    @Schema(description = "Бонус мастерства")
    private short proficiencyBonus;
    @Schema(description = "Уровень опасности в логове")
    private ChallengeRatingDto lair;
}

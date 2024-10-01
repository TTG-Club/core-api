package club.ttg.dnd5.dto.character;

import club.ttg.dnd5.dto.ChangedDto;
import club.ttg.dnd5.dto.NameDto;
import club.ttg.dnd5.dto.base.SourceDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)

@NoArgsConstructor
@AllArgsConstructor

@Getter
@Builder
public class ClassResponse {
    @Schema(description = "уникальный url")
    private String url;
    @Schema(description = "название")
    private NameDto name;
    @Schema(description = "хит дайсы")

    private String equipment;
    private String armorMastery;
    private String weaponMastery;
    private String toolMastery;

    private SourceDto source;

    private String hitDice;
    @Schema(description = "дата создания и последнего обновления")
    private ChangedDto changed;
}

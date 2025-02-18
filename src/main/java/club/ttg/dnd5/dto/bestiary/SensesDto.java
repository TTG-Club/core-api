package club.ttg.dnd5.dto.bestiary;

import club.ttg.dnd5.dto.NameValueDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Чувства")
public class SensesDto {
    @Schema(description = "Пассивная внимательность")
    private short passivePerception;
    @Schema(description = "Слепое зрение")
    private NameValueDto blindsight;
    @Schema(description = "Темное зрение")
    private NameValueDto darkvision;
    @Schema(description = "Истинное зрение")
    private NameValueDto truesight;
    @Schema(description = "Чувство вибрации")
    private NameValueDto tremorsense;
}

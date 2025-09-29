package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.ClassTableColumn;
import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import club.ttg.dnd5.domain.common.rest.dto.select.DiceOptionDto;
import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassDetailedResponse extends BaseResponse {

    @Schema(description = "Кость хитов класса с опциями")
    private DiceOptionDto hitDice;

    @Schema(description = "Владения класса")
    private ClassProficiencyDto proficiency;

    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    @Schema(description = "Снаряжение класса в формате Markdown")
    private String equipment;

    @Schema(description = "Спасброски класса")
    private String savingThrows;

    @Schema(description = "Список особенностей класса")
    private List<ClassFeatureDto> features;

    @Schema(description = "Таблица прогрессии класса")
    private List<ClassTableColumn> table;

    @Schema(description = "Тип заклинателя", example = "FULL")
    private CasterType casterType;
}

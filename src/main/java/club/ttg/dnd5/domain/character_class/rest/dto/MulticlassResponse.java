package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.ClassTableColumn;
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
public class MulticlassResponse  {

    @Schema(description = "Основные характеристики")
    private String primaryCharacteristics;

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

    private int characterLevel;
    private int spellcastingLevel;
    private List<MulticlassInfo> multiclass;
}

package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.ClassTableColumn;
import club.ttg.dnd5.domain.character_class.model.MulticlassProficiency;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Delimiter;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassRequest extends BaseRequest {
    @Schema(description = "URL родительского класса (если есть наследование)")
    private String parentUrl;

    @Schema(description = "Кость хитов класса")
    private Dice hitDice;

    @Schema(description = "Основные характеристики")
    private Set<Ability> primaryCharacteristics;

    @Schema(description = "Разделитель для основные характеристик")
    private Delimiter delimiterPrimary;

    @Schema(description = "Владение спасбросками")
    private Set<Ability> savingThrows;

    @Schema(description = "Владения класса")
    private ClassProficiencyRequest proficiency;

    @Schema(description = "Владения мультикласса")
    private MulticlassProficiency multiclassProficiency;

    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    @Schema(description = "Снаряжение класса в формате Markdown")
    private String equipment;

    @Schema(description = "Особенности класса")
    private List<ClassFeatureRequest> features;

    @Schema(description = "Колонки таблицы прогрессии класса")
    private List<ClassTableColumn> table;

    @Schema(description = "Тип заклинателя для отрисовки таблицы ячеек")
    private CasterType casterType;

    @Schema(description = "Шаблон распределения характеристик")
    private List<Integer> abilityTemplate;
}

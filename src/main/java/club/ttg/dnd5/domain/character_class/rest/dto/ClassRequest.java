package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.mechanics.ClassMechanics;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.model.EquipmentOption;
import club.ttg.dnd5.domain.character_class.model.ClassTableColumn;
import club.ttg.dnd5.domain.character_class.model.MulticlassProficiency;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import club.ttg.dnd5.dto.base.serializer.FormattedMarkupDescriptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
    private PrimaryAbilitiesDto primaryCharacteristics;

    @Schema(description = "Владение спасбросками")
    private Set<Ability> savingThrows;

    @Schema(description = "Владения класса")
    private ClassProficiencyRequest proficiency;

    @Schema(description = "Владения мультикласса")
    private MulticlassProficiency multiclassProficiency;

    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    @JsonSerialize(using = FormattedMarkupDescriptionSerializer.class)
    @Schema(description = "Снаряжение класса в формате Markdown")
    private String equipment;

    @Schema(description = "Стартовое снаряжение вариантами выбора: предметы с количеством и монеты")
    private List<EquipmentOption> startingEquipment;

    @Schema(description = "Особенности класса")
    private List<ClassFeatureRequest> features;

    @Schema(description = "Колонки таблицы прогрессии класса")
    private List<ClassTableColumn> table;

    @Schema(description = "Тип заклинателя для отрисовки таблицы ячеек")
    private CasterType casterType;

    @Schema(description = "Характеристика, которой класс колдует")
    private Ability spellcastingAbility;

    @Schema(description = "Уровень класса, с которого работает заклинательство", example = "3")
    private Integer spellcastingStartLevel;

    @Schema(description = "Подпись группы подклассов", example = "Воинский архетип")
    private String subclassLabel;

    @Schema(description = "Уровень, на котором выбирается подкласс", example = "3")
    private Integer subclassLevel;

    @Schema(description = "Механика влияния класса на лист персонажа")
    private ClassMechanics mechanics;

    @Schema(description = "Активные эффекты класса в вокабуляре VTTG")
    private List<ActiveEffect> activeEffects;

    @Schema(description = "Шаблон распределения характеристик")
    private List<Integer> abilityTemplate;
}

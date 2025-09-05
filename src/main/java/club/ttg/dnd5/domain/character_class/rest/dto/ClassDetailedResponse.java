package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.character_class.model.ArmorProficiency;
import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.ClassFeature;
import club.ttg.dnd5.domain.character_class.model.ClassTableColumn;
import club.ttg.dnd5.domain.character_class.model.WeaponProficiency;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import club.ttg.dnd5.domain.common.rest.dto.select.DiceOptionDto;
import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassDetailedResponse extends BaseResponse {

    @Schema(description = "Кость хитов класса с опциями")
    private DiceOptionDto hitDice;

    @Schema(description = "Владение доспехами", example = "Лёгкие и средние доспехи, щиты")
    private String armorProficiency;

    @Schema(description = "Владение оружием", example = "Простое и воинское оружие")
    private String weaponProficiency;

    @Schema(description = "Владение инструментами", example = "Ремесленные инструменты")
    private String toolProficiency;

    @Schema(description = "Владение навыками", example = "Выберите два навыка из следующих: Атлетика, Выживание...")
    private String skillProficiency;

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

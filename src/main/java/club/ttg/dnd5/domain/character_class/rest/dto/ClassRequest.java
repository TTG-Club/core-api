package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.character_class.model.ArmorProficiency;
import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.model.ClassFeature;
import club.ttg.dnd5.domain.character_class.model.ClassTableColumn;
import club.ttg.dnd5.domain.character_class.model.SkillProficiency;
import club.ttg.dnd5.domain.character_class.model.WeaponProficiency;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class ClassRequest extends BaseRequest {

    @Schema(description = "URL родительского класса (если есть наследование)")
    private String parentUrl;

    @Schema(description = "Кость хитов класса")
    private Dice hitDice;

    @Schema(description = "Владение спасбросками")
    private Set<Ability> savingThrows;

    @Schema(description = "Владение доспехами")
    private ArmorProficiency armorProficiency;

    @Schema(description = "Владение оружием")
    private WeaponProficiency weaponProficiency;

    @Schema(description = "Владение инструментами",
            example = "Ремесленные инструменты, воровские инструменты")
    private String toolProficiency;

    @Schema(description = "Владение навыками")
    private SkillProficiency skillProficiency;

    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    @Schema(description = "Снаряжение класса в формате Markdown")
    private String equipment;

    @Schema(description = "Особенности класса")
    private List<ClassFeature> features;

    @Schema(description = "Колонки таблицы прогрессии класса")
    private List<ClassTableColumn> table;

    @Schema(description = "Тип заклинателя для отрисоввки таблицы ячеек")
    private CasterType casterType;
}

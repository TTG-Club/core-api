package club.ttg.dnd5.domain.spell.rest.dto.create;

import club.ttg.dnd5.domain.beastiary.model.action.AttackType;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import club.ttg.dnd5.domain.spell.model.AreaOfEffect;
import club.ttg.dnd5.domain.spell.model.SpellCastingTime;
import club.ttg.dnd5.domain.spell.model.SpellComponents;
import club.ttg.dnd5.domain.spell.model.SpellDistance;
import club.ttg.dnd5.domain.spell.model.SpellDuration;
import club.ttg.dnd5.domain.common.dictionary.HealingType;
import club.ttg.dnd5.domain.spell.model.SpellSchool;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SpellRequest extends BaseRequest {
    @Schema(description = "Уровень заклинания")
    @Min(0)
    @Max(9)
    @NotNull
    private Long level;

    @Schema(description = "Школа магии")
    @NotNull
    private SpellSchool school;

    @Schema(description = "Требуемые компоненты")
    @NotNull
    private SpellComponents components;

    @Schema(description = "Время накладывания")
    @NotEmpty
    private List<SpellCastingTime> castingTime;

    @Schema(description = "Дистанция")
    @NotEmpty
    private List<SpellDistance> range;

    @Schema(description = "Длительность")
    @NotEmpty
    private List<SpellDuration> duration;

    @Schema(description = "На высоких уровнях")
    @Nullable
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String upper;

    @Schema(description = "Связанные сущности")
    @Nullable
    private CreateAffiliationRequest affiliations;

    @Schema(description = "Спасброски")
    @Nullable
    private List<Ability> savingThrow;

    @Schema(description = "Типы лечения")
    @Nullable
    private List<HealingType> healingType;

    @Schema(description = "Типы урона")
    @Nullable
    private List<DamageType> damageType;

    @Schema(description = "Накладываемые состояния")
    @Nullable
    private List<Condition> condition;

    @Schema(description = "Область действия эффекта если есть")
    @Nullable
    private AreaOfEffect areaOfEffect;

    @Schema(description = "Тип атаки если есть")
    @Nullable
    private AttackType attackType;
}

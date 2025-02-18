package club.ttg.dnd5.dto.bestiary;

import club.ttg.dnd5.dto.NameDto;
import club.ttg.dnd5.dto.NameValueDto;
import club.ttg.dnd5.dto.base.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class BeastDto extends BaseDTO {
    @Schema(description = "Размер существа (как правило один, но бывает и два)")
    private Collection<NameDto> sizes;
    private Collection<NameDto> types;
    @Schema(description = "Класс доспеха)")
    @JsonProperty(value = "AC")
    private byte armorClass;
    @Schema(description = "Хиты")
    @JsonProperty(value = "HP")
    private HitPointDto hitPoints;
    @Schema(description = "Скорости")
    private SpeedDto speed;
    @Schema(description = "Характеристики")
    private AbilitiesDto abilities;

    @Schema(description = "Навыки")
    private Collection<NameValueDto> skills;
    @Schema(description = "Сопротивления")
    private Collection<NameDto> resistances;
    @Schema(description = "Иммунитеты")
    private Collection<NameDto> immunities;
    @Schema(description = "Cнаряжение")
    private Collection<NameValueDto> gear;
    @Schema(description = "Чувства")
    private SensesDto senses;
    private Collection<NameDto> languages;
    @Schema(description = "Уровень опасности")
    @JsonProperty(value = "CR")
    private ChallengeRatingDto challengeRating;

}

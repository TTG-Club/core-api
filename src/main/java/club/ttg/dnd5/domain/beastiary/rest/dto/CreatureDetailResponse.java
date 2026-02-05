package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Setter
@Getter
public class CreatureDetailResponse extends BaseResponse {
    @Schema(description = "Тип, размер и мировоззрение существа")
    private String header;
    @JsonProperty("ac")
    @Schema(description = "Класс доспеха", examples = {"18"})
    private String armorClass;
    @Schema(description = "Инициатива", examples = {"+12 (22)"})
    private BonusDto initiative;
    @Schema(description = "Хиты")
    private HitResponse hit;
    @Schema(description = "Скорость")
    private String speed;

    @Schema(description = "Характеристики")
    private AbilitiesResponse abilities;
    @Schema(description = "Навыки")
    private Collection<BonusDto> skills;
    @Schema(description = "Уязвимости")
    private String vulnerability;
    @Schema(description = "Сопротивление")
    private String resistance;
    @Schema(description = "Иммунитет")
    private String immunity;
    @Schema(description = "Снаряжение")
    private String equipments;
    @Schema(description = "Чувства")
    private String sense;
    @Schema(description = "Языки", examples = "глубинный язык; телепатия 120 фт.")
    private String languages;

    @JsonProperty("cr")
    @Schema(name = "cr", description = "Показатель опасности", examples = "6 (Опыт 2300; БМ +3)")
    private String challengeRailing;

    @Schema(description = "Особенности")
    private Collection<TraitResponse> traits;

    @Schema(description = "Действия")
    private Collection<ActionResponse> actions;
    @Schema(description = "Реакции")
    private Collection<ActionResponse> reactions;
    @Schema(description = "Бонусные действия")
    private Collection<ActionResponse> bonusActions;

    @Schema(description ="Легендарные действия")
    private LegendaryActionResponse legendary;

    @Schema(description ="Описание логова")
    private CreatureLairResponse lair;

    @Schema(description = "Описании общей секции")
    private CreatureSectionResponse section;
}

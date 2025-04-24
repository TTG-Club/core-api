package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Setter
@Getter
public class BeastDetailResponse extends BaseResponse {
    @Schema(description = "Тип, размер и мировоззрение существа")
    private String header;
    @JsonProperty("AC")
    @Schema(description = "Класс доспеха", examples = {"18"})
    private String armorClass;
    @Schema(description = "Инициатива", examples = {"+12 (22)"})
    private String initiative;
    @Schema(description = "Хиты")
    private BeastHitResponse hit;
    @Schema(description = "Скорость")
    private String speed;

    @Schema(description = "Характеристики")
    private AbilitiesResponse abilities;

    @Schema(description = "Языки", examples = "глубинный язык; телепатия 120 фт.")
    private String languages;

    @JsonProperty("CR")
    @Schema(name = "CR", description = "Показатель опасности", examples = "6 (Опыт 2300; БМ +3)")
    private String challengeRailing;

    @Schema(description = "Особенности")
    private Collection<BeastTraitResponse> traits;

    @Schema(description = "Действия")
    private Collection<BeastActionResponse> actions;
    @Schema(description = "Реакции")
    private Collection<BeastActionResponse> reactions;
    @Schema(description = "Бонусные действия")
    private Collection<BeastActionResponse> bonusActions;
    @Schema(description = "Легендарные действия")
    private Collection<BeastActionResponse> legendaryActions;
}

package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.beastiary.model.BeastAction;
import club.ttg.dnd5.domain.beastiary.model.BeastTrait;
import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Setter
@Getter
public class BeastDetailResponse extends BaseResponse {
    @Schema(description = "Тип, размер и мировоззрение существа")
    private String header;
    @Schema(description = "Класс доспеха", examples = {"18"})
    private String armorClass;
    @Schema(description = "Инициатива", examples = {"+12 (22)"})
    private String initiative;
    @Schema(description = "Хиты")
    private BeastHitDto hit;

    @Schema(name = "CR", description = "Уровень опасности")
    private String challengeRailing;

    @Schema(description = "Особенности")
    private Collection<BeastTraitDto> traits;

    @Schema(description = "Действия")
    private Collection<BeastActionDto> actions;
    @Schema(description = "Реакции")
    private Collection<BeastActionDto> reactions;
    @Schema(description = "Бонусные действия")
    private Collection<BeastActionDto> bonusActions;
    @Schema(description = "Легендарные действия")
    private Collection<BeastActionDto> legendaryActions;
}

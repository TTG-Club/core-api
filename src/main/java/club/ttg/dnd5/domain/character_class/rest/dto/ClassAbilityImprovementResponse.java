package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.character_class.model.AbilityBonusResponse;
import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClassAbilityImprovementResponse extends ShortResponse {
    private List<Integer> levels;
    @Schema(description = "Шаблон распределения характеристик")
    private List<Integer> abilityTemplate;
    @Schema(description = "Бонусы к увеличивает характеристик")
    private List<AbilityBonusResponse> abilityBonus;
}

package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClassAbilityImprovementResponse extends ShortResponse {
    private List<Integer> levels;
}

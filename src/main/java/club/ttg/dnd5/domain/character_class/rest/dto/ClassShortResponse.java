package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import club.ttg.dnd5.domain.common.rest.dto.select.DiceOptionDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassShortResponse extends BaseResponse {
    @Schema(description = "Кость хитов класса")
    private DiceOptionDto hitDice;
    @Schema(description = "Подклассы")
    private List<ClassShortResponse> subclasses;
}

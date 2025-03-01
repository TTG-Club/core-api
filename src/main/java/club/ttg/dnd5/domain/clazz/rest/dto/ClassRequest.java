package club.ttg.dnd5.domain.clazz.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClassRequest extends BaseDto {
    @Schema(description = "Название класса в родительном падеже.", examples = "волшебника")
    private String genitive;
    @Schema(description = "Кость хитов")
    private String hitDice;
}

package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CreatureShortResponse extends ShortResponse {
    @Schema(name = "cr", description = "Уровень опасности")
    private String challengeRailing;
    @Schema(description = "Тип (или типы) существ")
    private String type;
    @Schema(description = "Опыт за победу над существом")
    private Long experience;
    @Schema(description = "Опыт за победу над существом в его логове; нет, если логова нет")
    private Long experienceInLair;
    @Schema(description = "Коды типов существа", examples = {"HUMANOID", "UNDEAD"})
    private List<String> typeCodes;
    @Schema(description = "Уточнение типа существа", examples = {"гоблиноид"})
    private String subtype;
    @Schema(description = "Семейство существа — название секции бестиария", examples = {"Гоблины"})
    private String family;
    @Schema(description = "Мировоззрение", examples = {"CHAOTIC_EVIL"})
    private String alignment;
    @Schema(description = "Спутники — url существ, с которыми это существо обычно встречается")
    private List<String> companions;
}

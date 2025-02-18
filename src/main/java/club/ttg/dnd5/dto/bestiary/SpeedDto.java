package club.ttg.dnd5.dto.bestiary;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Schema(description = "Скорости")
public class SpeedDto {
    @Schema(description = "Скорость перемещения по твердой горизонтальной поверхности")
    private short walk;
    @Schema(description = "Скорость перемещения сквозь грунт")
    private Short burrow;
    @Schema(description = "Скорость полета")
    private Short fly;
    @Schema(description = "Скорость плавания")
    private Short swim;
    @Schema(description = "Скорость лазания")
    private Short climb;
    @Schema(description = "Парение")
    private Boolean hover;
    @Schema(description = "Текстовое описание скорости")
    private String text;
}

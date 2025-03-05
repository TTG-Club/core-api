package club.ttg.dnd5.domain.species.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovementAttributes {
    @Schema(description = "Скорость")
    private final int base = 30;
    @Schema(description = "Скорость полета")
    private Integer fly;
    @Schema(description = "Скорость лазания")
    private Integer climb;
    @Schema(description = "Скорость плавания")
    private Integer swim;
    @Schema(description = "Истина если может парить")
    private Boolean hover;
}

package club.ttg.dnd5.domain.feat.model.mechanics;

import club.ttg.dnd5.domain.common.dictionary.SenseType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Чувство с дистанцией в футах: слепое зрение 10 у «Проныры» и «Сражения вслепую»,
 * истинное зрение 60 у «Дара истинного зрения».
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SenseGrant {
    @Schema(description = "Чувство", example = "BLINDSIGHT")
    private SenseType type;

    @Schema(description = "Дистанция в футах", example = "10")
    private Integer range;
}

package club.ttg.dnd5.domain.update.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LastUpdate extends BaseResponse {
    @Schema(description = "Обновлено или добавлено")
    private ChangeAction action;
}

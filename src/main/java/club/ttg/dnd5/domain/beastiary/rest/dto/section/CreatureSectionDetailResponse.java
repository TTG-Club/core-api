package club.ttg.dnd5.domain.beastiary.rest.dto.section;

import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureDetailResponse;
import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class CreatureSectionDetailResponse extends BaseResponse {
    @Schema(description = "Подзаголовок секции", examples = {"Вечный инопланетный разум"})
    private String subtitle;
    @Schema(description = "Среда обитания", examples = {"Подземье"})
    private String habitats;
    @Schema(description = "Сокровища", examples = {"Реликвии"})
    private String treasure;
    @Schema(description = "Существа входящие в секцию")
    private Collection<CreatureDetailResponse> creatures;
}
